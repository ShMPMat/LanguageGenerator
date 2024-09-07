package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException


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

    throw LanguageException("Cannot merge Phoneme matchers '$first' and '$second'")
}

