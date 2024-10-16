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
        is AbsentModifierPhonemeMatcher ->
            language.phonemeContainer.getPhonemesNot(matcher.modifiers).isNotEmpty()
        is ModifierPhonemeMatcher ->
            language.phonemeContainer.getPhonemes(matcher.modifiers).isNotEmpty()
        is BorderPhonemeMatcher, PassingPhonemeMatcher ->
            true
        else -> throw LanguageException("Unknown PhonemeMatcher '$matcher'")
    }
}

fun createDefaultRules(phonemeContainer: PhonemeContainer): PhonologicalRulesContainer {
    val noSyllableStructureChangeRules = listOf(
        // Vowel shifts
        "a -> o / _ ",
        "o -> a / _ ",
        "ə -> o / _ ",
        "ɨ -> o / _ ",
        // Consonant shifts
        "d -> g / _ n",
        "s -> h / _ ",

        // Vowel quality transfers
        "Vh -> [+Long]- / _ ",
        "Vx -> [+Long]- / _ ",
        "Və -> [+Long]- / _ ",
        // Consonant quality transfers
        "[-Voiced] -> [+Voiced] / _ V",
        "k -> [+Voiced] / _ V",
        "p -> [+Voiced] / _ V",
        "t -> [+Voiced] / _ V",
    ).map { createPhonologicalRule(it, phonemeContainer) }

    val possibleSyllableStructureChangeRules = listOf(
        // Vowel deletion
        "V -> - / \$C _ CV",
        "V -> - / VC _ C\$",
        // Consonant deletion
        "C -> - / _ \$",
    ).map { createPhonologicalRule(it, phonemeContainer) }
        .flatMap { listOf(it, it.copy(allowSyllableStructureChange = true)) }

    return PhonologicalRulesContainer(
        noSyllableStructureChangeRules + possibleSyllableStructureChangeRules
    )
}
