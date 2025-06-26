package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class InterconsonanticVowelInsertion : MatcherConditionPhonologicalChangeTendency() {
    override fun getNewInstance() = InterconsonanticVowelInsertion()

    override val divisionCoefficient: Double = 1000.0

    override fun createMatchers(phonemeContainer: PhonemeContainer): List<List<PhonemeMatcher>> =
        listOf(
            createPhonemeMatchers("CC", phonemeContainer)
        )

    // No use trying to do more after the 1st attempt
    override fun computeRetentionChance(language: Language): Double = 0.0

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            allowSyllableStructureChange = true

            return (createRules(" -> (<V>) / C _ C") allWithProb 3.0) +
                    (createRules(" -> (ə) / C _ C") allWithProb 0.1)
        }
}
