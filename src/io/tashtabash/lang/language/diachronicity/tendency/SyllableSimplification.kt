package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class SyllableSimplification : UnionPhonologicalChangeTendency() {
    override fun getNewInstance() = SyllableSimplification()

    override val tendencies = listOf(InterconsonanticVowelInsertion(), Nasalization(), VowelSequenceSimplification())
}


private class Nasalization : OptionListPhonologicalChangeTendency() {
    override fun getNewInstance() = Nasalization()

    private var regressiveChance: Double? = null
    private var progressiveChance: Double? = null

    override fun computeDevelopmentChance(language: Language): Double {
        if (appliedRules.isNotEmpty())
            return .0

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
    override fun computeRetentionChance(language: Language): Double = .0

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return (createWeakRules("(V[-Nasalized])(C[+Nasal]) -> [+Nasalized]- / _ ") allWithProb .1) +
                    (createWeakRules("(C[+Nasal])(V[-Nasalized]) -> -[+Nasalized] / _ ") allWithProb .1)
        }
}


class VowelSequenceSimplification : OptionListPhonologicalChangeTendency() {
    override fun getNewInstance() = VowelSequenceSimplification()

    override fun computeDevelopmentChance(language: Language): Double =
        1.0

    override fun computeRetentionChance(language: Language): Double = .1

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return (createWeakRules("VV -> _- / _ ") allWithProb .1) +
                    (createWeakRules("VV -> -_ / _ ") allWithProb .1) +
                    (createWeakRules("VV -> [+Long]_ / _ ") allWithProb .1) +
                    (createWeakRules("VV -> -[+Long] / _ ") allWithProb .1)
        }
}


class ConsonantSequenceSimplification : OptionListPhonologicalChangeTendency() {
    override fun getNewInstance() = ConsonantSequenceSimplification()

    override fun computeDevelopmentChance(language: Language): Double =
        1.0

    override fun computeRetentionChance(language: Language): Double = .1

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return (createWeakRules("CC -> _- / _ ") allWithProb .1) +
                    (createWeakRules("CC -> -_ / _ ") allWithProb .1) +
                    (createWeakRules("CC -> [+Long]_ / _ ") allWithProb .1) +
                    (createWeakRules("CC -> -[+Long] / _ ") allWithProb .1)
        }
}


class ConsonantDegemination : OptionListPhonologicalChangeTendency() {
    override fun getNewInstance() = ConsonantDegemination()

    override fun computeDevelopmentChance(language: Language): Double =
        language.phonemeContainer
            .getPhonemes(PhonemeModifier.Long)
            .filter { it.type == PhonemeType.Consonant }
            .size
            .toDouble()

    override fun computeRetentionChance(language: Language): Double = .1

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return createWeakRules("(C[+Long]) -> [-Long] / _ ") allWithProb .1
        }
}
