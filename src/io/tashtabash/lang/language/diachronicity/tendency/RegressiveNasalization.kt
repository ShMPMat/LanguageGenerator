package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class RegressiveNasalization : MatcherConditionPhonologicalChangeTendency() {
    override fun getNewInstance() = RegressiveNasalization()

    override fun createMatchers(phonemeContainer: PhonemeContainer): List<List<PhonemeMatcher>> =
        listOf(
            createPhonemeMatchers("(V[-Nasalized])(C[+Nasal])", phonemeContainer)
        )

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return (createWeakRules("(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]- / _ ") allWithProb 0.1) +
                    (createWeakRules("(V[-Nasalized]) -> [+Nasalized] / _ (C[+Nasal])") allWithProb 0.1)
        }
}
