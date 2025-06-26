package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class VowelShortening : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = VowelShortening()

    override fun computeDevelopmentChance(language: Language): Double =
        language.phonemeContainer
            .getPhonemes(PhonemeType.Vowel)
            .count { PhonemeModifier.Long in it.characteristics }
            .toDouble()

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return (createWeakRules("${escapeStress("<(V[+Long])>")} -> [-Long] / _ ") allWithProb 0.1) +
                    (createWeakRules("${escapeStress("<(V[+Long])>")} -> [-Long] / [-Voiced] _ [-Voiced]") allWithProb 0.1) +
                    (createWeakRules("<(V[+Long])> -> [-Long] / _ ") allWithProb 0.01) +
                    (createWeakRules("<(V[+Long])> -> [-Long] / [-Voiced] _ [-Voiced]") allWithProb 0.01)
        }
}
