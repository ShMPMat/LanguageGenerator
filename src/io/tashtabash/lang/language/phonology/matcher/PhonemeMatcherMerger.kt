package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import kotlin.math.max


fun unitePhonemeMatchers(first: List<PhonemeMatcher>, second: List<PhonemeMatcher>): List<PhonemeMatcher?> {
    val newMatchersLength = max(first.size, second.size)

    return (0 until newMatchersLength).map { j ->
        unitePhonemeMatchers(
            first.getOrNull(j),
            second.getOrNull(j)
        )
    }
}

// Returns null if the resulting union matches no phonemes
fun unitePhonemeMatchers(first: PhonemeMatcher?, second: PhonemeMatcher?): PhonemeMatcher? {
    first ?: return second
    second ?: return first

    val phonemeMatchers = listOf(first, second)

    if (phonemeMatchers.any { it is PassingPhonemeMatcher})
        return phonemeMatchers.firstOrNull { it !is PassingPhonemeMatcher }
            ?: PassingPhonemeMatcher

    if (phonemeMatchers.all { it is BorderPhonemeMatcher})
        return BorderPhonemeMatcher
    if (phonemeMatchers.any { it is BorderPhonemeMatcher})
        return null

    if (phonemeMatchers.all { it is ExactPhonemeMatcher} && first == second)
        return first
    if (phonemeMatchers.any { it is ExactPhonemeMatcher } && phonemeMatchers.any { it is TypePhonemeMatcher }) {
        val exactPhonemeMatcher = phonemeMatchers.filterIsInstance<ExactPhonemeMatcher>()
            .first()
        val typePhonemeMatcher = phonemeMatchers.filterIsInstance<TypePhonemeMatcher>()
            .first()

        if (exactPhonemeMatcher.phoneme.type == typePhonemeMatcher.phonemeType)
            return exactPhonemeMatcher

        return null
    }
    if (phonemeMatchers.any { it is ExactPhonemeMatcher } && phonemeMatchers.any { it is AbsentModifierPhonemeMatcher }) {
        val exactPhonemeMatcher = phonemeMatchers.filterIsInstance<ExactPhonemeMatcher>()
            .first()
        val absentModifierPhonemeMatcher = phonemeMatchers.filterIsInstance<AbsentModifierPhonemeMatcher>()
            .first()

        if (absentModifierPhonemeMatcher.match(exactPhonemeMatcher.phoneme))
            return exactPhonemeMatcher

        return null
    }

    if (phonemeMatchers.all { it is TypePhonemeMatcher }) {
        return if (first == second)
            first
        else
            null
    }

    if (phonemeMatchers.all { it is AbsentModifierPhonemeMatcher })
        AbsentModifierPhonemeMatcher(
            (first as AbsentModifierPhonemeMatcher).modifiers
                    + (second as AbsentModifierPhonemeMatcher).modifiers
        )

    throw LanguageException("Cannot merge Phoneme matchers '$first' and '$second'")
}

