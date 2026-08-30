package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb
import io.tashtabash.random.withProb


class Palatalization : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = Palatalization()

    override fun computeDevelopmentChance(language: Language): Double =
        .1

    override val defaultRetentionChance = .8

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return listOf(
                createWeakRules("(C[-Palatalized][-Palatal]) -> [+Palatalized] / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("(C[-Palatalized][-Palatal]) -> [+Palatalized] / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("(C[-Palatalized][-Palatal]) -> [+Palatalized] / _ j") allWithProb .3,
                createWeakRules("(C[-Palatalized][-Palatal]) -> [+Palatalized] / j _ ") allWithProb .1,
                createWeakRules("(C[-Palatalized][-Palatal]) -> [+Palatalized] / [+Palatalized] _ ") allWithProb .1,
                createWeakRules("(C[-Palatalized][-Palatal]) -> [+Palatalized] / _ [+Palatalized]") allWithProb .1,
                // With changing the place of articulation
                createWeakRules("k -> |tʃ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("k -> |tʃ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("k -> |tʃ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("k -> |tʃ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("k -> |ts| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("k -> |ts| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("k -> ʃ / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("k -> ʃ / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("k -> s / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("k -> s / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("t -> |tʃ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("t -> |tʃ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("t -> |tɕ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("t -> |tɕ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("t -> |ts| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("t -> |ts| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("t -> |tsʲ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("t -> |tsʲ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("g -> |tʃ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("g -> |tʃ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("g -> |tʃ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("g -> |ts| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("g -> |ts| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("g -> ʃ / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("g -> ʃ / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("g -> s / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("g -> s / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("d -> |tʃ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("d -> |tʃ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("d -> |tɕ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("d -> |tɕ| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("d -> |ts| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("d -> |ts| / (V[+Front][-Open]) _ ") allWithProb .1,
                createWeakRules("d -> |tsʲ| / _ (V[+Front][-Open])") allWithProb .3,
                createWeakRules("d -> |tsʲ| / (V[+Front][-Open]) _ ") allWithProb .1,
                // With vowel reduction
                createWeakRules("(C[-Palatalized][-Palatal])(V[+Front][-Open]) -> [+Palatalized]ə / _ ") allWithProb .3,
                createWeakRules("(V[+Front][-Open])(C[-Palatalized][-Palatal]) -> ə[+Palatalized] / _ ") allWithProb .1,
            ).flatten()
        }
}


class Depalatalization : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = Depalatalization()

    override fun computeDevelopmentChance(language: Language): Double =
        language.phonemeContainer
            .getPhonemes(PhonemeModifier.Palatalized)
            .size
            .toDouble()

    override val defaultRetentionChance = .8

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return listOf(
                createRule("(C[+Palatalized]) -> [-Palatalized] / _ ") withProb .5,
                createRule("(C[+Palatalized]) -> [-Palatalized] / _ $") withProb .1,
                createRule("(C[+Palatalized]) -> [-Palatalized] / $ _ ") withProb .1,
                createRule("(C[+Palatalized]) -> [-Palatalized] / [-Palatalized] _ ") withProb .1,
                createRule("(C[+Palatalized]) -> [-Palatalized] /  _ [-Palatalized]") withProb .1,
            )
        }
}
