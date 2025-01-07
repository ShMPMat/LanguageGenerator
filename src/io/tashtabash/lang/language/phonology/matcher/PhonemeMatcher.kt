package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.morphem.change.substitution.DeletingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.prosody.Prosody


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
    mulModifierRegex.matches(matcher) -> MulMatcher(
        matcher.drop(1)
            .dropLast(1)
            .split("[\\[{]".toRegex())
            .map { restoreInitialBracket(it) }
            .map { createPhonemeMatcher(it, phonemeContainer) }
    )
    else -> {
        val phoneme = phonemeContainer.getPhonemeOrNull(matcher)
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

fun unitePhonemeMatchersAfterSubstitution(
    first: List<PhonemeMatcher>,
    // Substitutions applied after the first matchers, used to correctly determine shifts
    firstSubstitutions: List<PhonemeSubstitution>,
    second: List<PhonemeMatcher>,
): List<PhonemeMatcher?> {
    val shifts = firstSubstitutions.map { it is DeletingPhonemeSubstitution }

    return unitePhonemeMatchers(first, second, shifts)
}

fun unitePhonemeMatchers(
    first: List<PhonemeMatcher>,
    second: List<PhonemeMatcher>,
    // If true, the corresponding position in first will be skipped.
    //  Useful for merging Matchers, in-between which a substitution is applied.
    shifts: List<Boolean> = listOf()
): List<PhonemeMatcher?> {
    var firstIdx = 0
    var secondIdx = 0
    val result = mutableListOf<PhonemeMatcher?>()

    while (firstIdx < second.size || secondIdx < first.size) {
        val curFirst = first.getOrNull(firstIdx)
        val curSecond = second.getOrNull(secondIdx)
        if (shifts.getOrNull(firstIdx) == true) {
            result += curFirst
            firstIdx++
            continue
        }

        result += curFirst?.times(curSecond)
            ?: curSecond?.times(curFirst)

        firstIdx++
        secondIdx++
    }

    return result
}
