package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class SyllableSimplification : UnionPhonologicalChangeTendency() {
    override fun getNewInstance() = SyllableSimplification()

    override val tendencies = listOf(InterconsonanticVowelInsertion(), Nasalization())
}


private class Nasalization : OptionListPhonologicalChangeTendency() {
    override fun getNewInstance() = Nasalization()

    private var regressiveChance: Double? = null
    private var progressiveChance: Double? = null

    override fun computeDevelopmentChance(language: Language): Double {
        if (appliedRules.isNotEmpty())
            return 0.0

        regressiveChance = regressiveChance
            ?: createPhonemeMatchers("(V[-Nasalized])(C[+Nasal])", language.phonemeContainer)
                .countMatchesUnique(language)
                .toDouble()
        progressiveChance = progressiveChance
            ?: createPhonemeMatchers("(C[+Nasal])(V[-Nasalized])", language.phonemeContainer)
                .countMatchesUnique(language)
                .toDouble()

        return (regressiveChance!! + progressiveChance!!) / 10000.0
    }

    // Discard after the first application
    override fun computeRetentionChance(language: Language): Double = 0.0

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return (createWeakRules("(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]- / _ ") allWithProb 0.1) +
                    (createWeakRules("(C[+Nasal])(V[-Nasalized]) -> -[+Nasalized] / _ ") allWithProb 0.1)
        }
}
