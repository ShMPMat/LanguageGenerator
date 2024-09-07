package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.phonology.matcher.*


data class PhonologicalRulesContainer(val phonologicalRules: List<PhonologicalRule>) {
    fun getApplicableRules(language: Language) = phonologicalRules.filter { rule ->
        rule.matchers.all { isMatcherApplicable(it, language) }
    }

    private fun isMatcherApplicable(matcher: PhonemeMatcher, language: Language) = when (matcher) {
        is ExactPhonemeMatcher ->
            language.phonemeContainer.getPhonemeOrNull(matcher.phoneme.symbol) != null
        is TypePhonemeMatcher ->
            language.phonemeContainer.getPhonemes(matcher.phonemeType).isNotEmpty()
        is BorderPhonemeMatcher, PassingPhonemeMatcher ->
            true
        else -> throw LanguageException("Unknown PhonemeMatcher '$matcher'")
    }
}

fun createDefaultRules(phonemeContainer: PhonemeContainer) = PhonologicalRulesContainer(
    listOf(
        "a -> o / _ ",
        "o -> a / _ ",
        "ə -> o / _ ",
        "ɨ -> o / _ ",

        "d -> g / _ n",

        "C -> - / _ $",
    ).map { createPhonologicalRule(it, phonemeContainer) }
)
