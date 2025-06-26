package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class VowelReduction : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = VowelReduction()

    override fun computeDevelopmentChance(language: Language): Double =
        language.phonemeContainer
            .getPhonemes(PhonemeType.Vowel)
            .size
            .toDouble()

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return (createRules("${escapeStress("<V>")} -> ə / _") allWithProb 1.0) +
                    // Schwa reduction
                    (createWeakRules("${escapeStress("ə")} -> - / _ ") allWithProb 0.03) +
                    // Between devoiced
                    (createWeakRules("${escapeStress("V")} -> - / [-Voiced] _ [-Voiced]") allWithProb 0.05) +
                    (createWeakRules("${escapeStress("V")} -> ə / [-Voiced] _ [-Voiced]") allWithProb 0.05) +
                    (createWeakRules("${escapeStress("ə")} -> - / [-Voiced] _ [-Voiced]") allWithProb 0.1)
        }
}
