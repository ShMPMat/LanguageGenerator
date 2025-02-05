package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.morphem.change.substitution.*
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.prosody.Prosody
import kotlin.math.min


abstract class PhonemeMatcher {
    abstract val name: String

    abstract fun match(phoneme: Phoneme?): Boolean

    abstract fun match(changingPhoneme: ChangingPhoneme): Boolean

    abstract operator fun times(other: PhonemeMatcher?): PhonemeMatcher?

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
    matcher == "_" -> PassingPhonemeMatcher
    matcher == "$" -> BorderPhonemeMatcher
    mulModifierRegex.matches(matcher) -> MulMatcher(
        matcher.drop(1)
            .dropLast(1)
            .split("[\\[{]".toRegex())
            .filter { it.isNotEmpty() }
            .map { restoreInitialBracket(it) }
            .map { createPhonemeMatcher(it, phonemeContainer) }
    )
    modifierRegex.matches(matcher) -> ModifierPhonemeMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { PhonemeModifier.valueOf(it) }
            .toSet()
    )
    absentModifierRegex.matches(matcher) -> AbsentModifierPhonemeMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { PhonemeModifier.valueOf(it) }
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
    first: List<PhonemeMatcher>,
    // Substitutions applied after the first matchers, used to correctly determine shifts
    firstSubstitutions: List<PhonemeSubstitution?>,
    second: List<PhonemeMatcher>,
    shift: Int = 0
): UnitePhonemeMatchersResult {
    var firstIdx = min(shift, 0)
    var substitutionShift = 0
    var secondIdx = min(-shift, 0)
    val result = mutableListOf<PhonemeMatcher?>()
    var isNarrowed = false
    var isChanged = false

    while (firstIdx < first.size || secondIdx < second.size) {
        val curFirst = first.getOrNull(firstIdx)
        val curFirstSubstitution = firstSubstitutions.getOrNull(firstIdx + substitutionShift)
        val curSecond = second.getOrNull(secondIdx)
        if (curFirstSubstitution != null)
            when (curFirstSubstitution) {
                is DeletingPhonemeSubstitution -> {
                    // curSecond should be compared with the next curFirst
                    result += curFirst
                    firstIdx++
                    continue
                }
                is EpenthesisSubstitution -> {
                    // If the new phoneme matches, do nothing
                    if (curSecond?.match(curFirstSubstitution.epenthesisPhoneme) != true)
                        result += null
                    substitutionShift++
                    secondIdx++
                    isChanged = true
                    continue
                }
                is ExactPhonemeSubstitution -> {
                    // If the new phoneme matches, then all is fine & curSecond will be matched; return curFirst
                    result +=
                        if (curSecond?.match(curFirstSubstitution.exactPhoneme) == true)
                            curFirst
                        else null
                    firstIdx++
                    secondIdx++
                    isChanged = true
                    continue
                }
                is ModifierPhonemeSubstitution -> { //TODO we need possible phoneme context
                    val possibleSubstitutionResults = curFirstSubstitution.phonemes
                        .phonemes
                        .filter { curFirst?.match(it) == true }
                        .map { it to curFirstSubstitution.substituteOrNull(it) }
                        .filter { it.second != null }
                    val matchingPhonemes = possibleSubstitutionResults.filter { curSecond?.match(it.second) == true }
                    result += if (matchingPhonemes.isEmpty())
                        null
                    else if (matchingPhonemes.size == 1)
                        ExactPhonemeMatcher(matchingPhonemes[0].first)
                    else
                        // There are a few possible matches for curSecond, so let's just match curFirst
                        //  and try applying the merged substitutions. This means that the new change
                        //  may fail to substitute phonemes, even if they were matched.
                        curFirst
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

        result += curFirst?.times(curSecond)
            ?: curSecond?.times(curFirst)

        // If the first PhonemeMatcher is null, it means that a completely new matcher is added
        if (curFirst == null)
            isNarrowed = true

        if (curFirst != null && curSecond != null)
            isChanged = true

        firstIdx++
        secondIdx++
    }

    return UnitePhonemeMatchersResult(isNarrowed, isChanged, result)
}

data class UnitePhonemeMatchersResult(
    val phonemeMatchers: List<PhonemeMatcher>?,
    val isNarrowed: Boolean,
    val isChanged: Boolean
) {
    constructor(isNarrowed: Boolean, isChanged: Boolean, phonemeMatchers: List<PhonemeMatcher?>): this(
        phonemeMatchers.takeIf { it.none { m -> m == null } }
            ?.filterNotNull(),
        isNarrowed,
        isChanged
    )
}
