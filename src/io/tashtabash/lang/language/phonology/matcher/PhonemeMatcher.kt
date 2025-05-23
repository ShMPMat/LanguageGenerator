package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.morphem.change.substitution.*
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.parseCharacteristic
import io.tashtabash.lang.language.phonology.prosody.Prosody
import kotlin.math.min


abstract class PhonemeMatcher {
    abstract val name: String

    abstract fun match(phoneme: Phoneme?): Boolean

    abstract fun match(changingPhoneme: ChangingPhoneme): Boolean

    /**
     * Returns a pair of a resulting Phoneme matcher and whether it is narrower than the original matcher
     */
    abstract operator fun times(other: PhonemeMatcher?): Pair<PhonemeMatcher, Boolean>?

    open fun any(predicate: (PhonemeMatcher) -> Boolean) =
        predicate(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhonemeMatcher

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = name
}

fun List<PhonemeMatcher>.match(phonemeWindow: List<ChangingPhoneme>): Boolean =
    zip(phonemeWindow)
        .all { (matcher, phoneme) -> matcher.match(phoneme) }

fun List<PhonemeMatcher>.matchAll(phonemes: List<ChangingPhoneme>): Boolean =
    (0..phonemes.size - size)
        .any { match(phonemes.drop(it)) }

fun createPhonemeMatchers(matchers: String, phonemeContainer: PhonemeContainer): List<PhonemeMatcher> {
    var currentPostfix = matchers
    val resultMatchers = mutableListOf<PhonemeMatcher>()

    while (currentPostfix.isNotEmpty()) {
        val token = if (currentPostfix[0] == '[')
            currentPostfix.takeWhile { it != ']' } + ']'
        else if (currentPostfix[0] == '(')
            currentPostfix.takeWhile { it != ')' } + ')'
        else if (currentPostfix[0] == '|')
            // Take a multi-char
            '|' + currentPostfix.drop(1).takeWhile { it != '|' } + '|'
        else
            currentPostfix.take(1)

        resultMatchers += createPhonemeMatcher(token, phonemeContainer)

        currentPostfix = currentPostfix.drop(token.length)
    }

    return resultMatchers
}

fun createPhonemeMatcher(matcher: String, phonemeContainer: PhonemeContainer): PhonemeMatcher = when {
    matcher == "C" -> TypePhonemeMatcher(PhonemeType.Consonant)
    matcher == "V" -> TypePhonemeMatcher(PhonemeType.Vowel)
    matcher == "*" -> PassingPhonemeMatcher
    matcher == "$" -> BorderPhonemeMatcher
    mulModifierRegex.matches(matcher) -> MulMatcher(
        matcher.drop(1)
            .dropLast(1)
            .split("[\\[{]".toRegex())
            .filter { it.isNotEmpty() }
            .map { restoreInitialBracket(it) }
            .map { createPhonemeMatcher(it, phonemeContainer) }
    )
    modifierRegex.matches(matcher) -> CharacteristicPhonemeMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { parseCharacteristic(it) }
            .toSet()
    )
    absentModifierRegex.matches(matcher) -> AbsentCharacteristicPhonemeMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { parseCharacteristic(it) }
            .toSet()
    )
    prosodyRegex.matches(matcher) -> ProsodyMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { Prosody.valueOf(it) }
            .toSet()
    )
    absentProsodyRegex.matches(matcher) -> AbsentProsodyMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { Prosody.valueOf(it) }
            .toSet()
    )
    else -> {
        val phoneme = phonemeContainer.getPhonemeOrNull(matcher.trim('|'))
            ?: throw LanguageException("cannot create a matcher for symbol '$matcher'")

        ExactPhonemeMatcher(phoneme)
    }
}

private fun restoreInitialBracket(token: String) = when (token.last()) {
    ']' -> "[$token"
    '}' -> "{$token"
    else -> token
}

private val modifierRegex = "\\[\\+.*]".toRegex()
private val prosodyRegex = "\\{\\+.*}".toRegex()
private val absentProsodyRegex = "\\{-.*}".toRegex()
private val absentModifierRegex = "\\[-.*]".toRegex()
private val mulModifierRegex = "\\(.*\\)".toRegex()


fun unitePhonemeMatchers(
    first: List<PhonemeMatcher?>,
    // Substitutions applied after the first matchers, used to correctly determine shifts
    firstSubstitutions: List<PhonemeSubstitution?>,
    second: List<PhonemeMatcher>,
    shift: Int = 0
): UnitePhonemeMatchersResult? {
    var firstIdx = min(shift, 0)
    var secondIdx = min(-shift, 0)
    val result = mutableListOf<PhonemeMatcher>()
    var isNarrowed = false
    var isChanged = false
    var applicationRange: IntRange? = null

    while (firstIdx < first.size || secondIdx < second.size) {
        val curFirst = first.getOrNull(firstIdx)
        val curFirstSubstitution = firstSubstitutions.getOrNull(firstIdx)
        val curSecond = second.getOrNull(secondIdx)

        // The first matcher was applied on the previous step
        if (secondIdx == 1)
            applicationRange = (firstIdx - 1) until firstIdx
        else if (secondIdx == second.size)
            applicationRange = applicationRange!!.first until firstIdx

        if (curFirstSubstitution != null)
            when (curFirstSubstitution) {
                is DeletingPhonemeSubstitution -> {
                    // curSecond should be compared with the next curFirst
                    result += curFirst
                        ?: return null
                    firstIdx++
                    continue
                }
                is EpenthesisSubstitution -> {
                    // If the new phoneme matches, do nothing
                    if (curSecond?.match(curFirstSubstitution.epenthesisPhoneme) == false)
                        return null
                    firstIdx++
                    secondIdx++
                    isChanged = true
                    continue
                }
                is ExactPhonemeSubstitution -> {
                    // If the new phoneme matches, then all is fine & curSecond will be matched; return curFirst
                    result +=
                        if (curSecond?.match(curFirstSubstitution.exactPhoneme) != false)
                            curFirst
                                ?: return null
                        else return null
                    firstIdx++
                    secondIdx++
                    isChanged = true
                    continue
                }
                is ModifierPhonemeSubstitution -> {
                    val possibleSubstitutionResults = curFirstSubstitution.phonemes
                        .phonemes
                        .filter { curFirst?.match(it) == true }
                        .map { it to curFirstSubstitution.substituteOrNull(it) }
                        .filter { it.second != null }
                    val matchingPhonemes = possibleSubstitutionResults.filter { curSecond?.match(it.second) == true }
                    result += if (matchingPhonemes.isEmpty())
                        return null
                    else if (matchingPhonemes.size == 1)
                        ExactPhonemeMatcher(matchingPhonemes[0].first)
                    else
                        // There are a few possible matches for curSecond, so let's just match curFirst
                        //  and try applying the merged substitutions. This means that the new change
                        //  may fail to substitute phonemes, even if they were matched.
                        curFirst
                            ?: return null
                    if (possibleSubstitutionResults.size != matchingPhonemes.size)
                        isNarrowed = true
                    firstIdx++
                    secondIdx++
                    isChanged = true
                    continue
                }
                is PassingPhonemeSubstitution -> {
                    // Go further and simply multiply the matchers
                }
                else -> {
                    throw LanguageException("Can't handle substitution '$curFirstSubstitution'")
                }
            }

        val (matcher, wasNarrowed) = curFirst?.times(curSecond)
            ?: curSecond?.times(curFirst)
            ?: return null
        result += matcher
        isNarrowed = isNarrowed || wasNarrowed

        // If the first PhonemeMatcher is null, it means that a completely new matcher is added
        if (curFirst == null)
            isNarrowed = true

        if (curFirst != null && curSecond != null)
            isChanged = true

        firstIdx++
        secondIdx++
    }

    // Possible only if the second has one matcher, and it's been applied the last
    if (applicationRange == null)
        applicationRange = (firstIdx - 1) until firstIdx
    // Possible only if the second's last matcher has been applied the last
    if (secondIdx == second.size)
        applicationRange = applicationRange.first until firstIdx

    // No BorderPhonemeMatcher in the middle
    if (result.drop(1).dropLast(1).any { it == BorderPhonemeMatcher })
        return null

    return UnitePhonemeMatchersResult(result, isNarrowed, isChanged, applicationRange)
}

data class UnitePhonemeMatchersResult(
    val phonemeMatchers: List<PhonemeMatcher>,
    val isNarrowed: Boolean,
    val isChanged: Boolean,
    val applicationRange: IntRange
)
