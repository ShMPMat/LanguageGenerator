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


class Labialization : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = Labialization()

    override fun computeDevelopmentChance(language: Language): Double =
        .1

    override val defaultRetentionChance = .8

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return listOf(
                createWeakRules("(C[-Labialized]) -> [+Labialized] / _ (V[+Labialized])") allWithProb .3,
                createWeakRules("(C[-Labialized]) -> [+Labialized] / (V[+Labialized]) _ ") allWithProb .1,
                createWeakRules("(C[-Labialized]) -> [+Labialized] / [+Labialized] _ ") allWithProb .1,
                createWeakRules("(C[-Labialized]) -> [+Labialized] / _ [+Labialized]") allWithProb .1,
                // With vowel reduction
                createWeakRules("(C[-Labialized])(V[+Labialized]) -> [+Labialized]ə / _ ") allWithProb .3,
                createWeakRules("(V[+Labialized])(C[-Labialized]) -> ə[+Labialized] / _ ") allWithProb .1,
            ).flatten()
        }
}


class Delabialization : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = Delabialization()

    override fun computeDevelopmentChance(language: Language): Double =
        language.phonemeContainer
            .getPhonemes(PhonemeModifier.Labialized)
            .filter { it.type == PhonemeType.Consonant }
            .size
            .toDouble()

    override val defaultRetentionChance = .8

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return listOf(
                createRule("(C[+Labialized]) -> [-Labialized] / _ ") withProb .5,
                createRule("(C[+Labialized]) -> [-Labialized] / _ $") withProb .1,
                createRule("(C[+Labialized]) -> [-Labialized] / $ _ ") withProb .1,
                createRule("(C[+Labialized]) -> [-Labialized] / [-Labialized] _ ") withProb .1,
                createRule("(C[+Labialized]) -> [-Labialized] /  _ [-Labialized]") withProb .1,
            )
        }
}
