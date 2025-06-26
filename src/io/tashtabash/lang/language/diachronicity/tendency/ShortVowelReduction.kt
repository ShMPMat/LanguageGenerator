package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb


class ShortVowelReduction : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = ShortVowelReduction()

    override fun computeDevelopmentChance(language: Language): Double =
        language.phonemeContainer
            .getPhonemes(PhonemeType.Vowel)
            .let { vowels ->
                // 0.0 if there are no Long or Short vowels, the biggest prob if their numbers are balanced
                vowels.count { PhonemeModifier.Long in it.modifiers } *
                        vowels.count { PhonemeModifier.Long !in it.modifiers } /
                        10.0
            }

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            return createWeakRules("${escapeStress("(V[-Long])")} -> ə / _ ") allWithProb 0.1
        }
}
