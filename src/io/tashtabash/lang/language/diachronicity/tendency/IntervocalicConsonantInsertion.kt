package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class IntervocalicConsonantInsertion : MatcherConditionPhonologicalChangeTendency() {
    override fun getNewInstance() = IntervocalicConsonantInsertion()

    override val divisionCoefficient: Double = 1000.0

    override fun createMatchers(phonemeContainer: PhonemeContainer): List<List<PhonemeMatcher>> =
        listOf(
            createPhonemeMatchers("VV", phonemeContainer)
        )

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            allowSyllableStructureChange = true

            return (createRules(" -> (<[+Approximant]>) / V _ V") allWithProb 10.0) +
                    (createRules(" -> (j) / V _ V") allWithProb 0.1) +
                    (createRules(" -> (w) / V _ V") allWithProb 0.1)
        }
}
