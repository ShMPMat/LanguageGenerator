package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb
import io.tashtabash.random.withProb


class Voicing : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = Voicing()

    override fun computeDevelopmentChance(language: Language): Double =
        language.phonemeContainer
            .getPhonemes(PhonemeModifier.Voiced)
            .count { it.type == PhonemeType.Consonant }
            .toDouble()

    override val defaultRetentionChance = 0.8

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return listOf(
                createRule("(C[-Voiced]) -> [+Voiced] / V _ ") withProb 0.1,
                createRule("(C[-Voiced]) -> [+Voiced] /  _ V") withProb 0.1,
                createRule("(C[-Voiced]) -> [+Voiced] / [+Voiced] _ ") withProb 0.1,
                createRule("(C[-Voiced]) -> [+Voiced] /  _ [+Voiced]") withProb 0.1,
            ) + (createWeakRules("(C[-Voiced]) -> [+Voiced] / V _ V") allWithProb 0.1)
        }
}
