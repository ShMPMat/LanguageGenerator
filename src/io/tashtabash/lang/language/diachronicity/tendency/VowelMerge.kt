package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.utils.composeUniquePairs
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomUnwrappedElementOrNull
import io.tashtabash.random.toSampleSpaceObject
import io.tashtabash.random.withProb


class VowelMerge : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = VowelMerge()

    override fun computeDevelopmentChance(language: Language): Double =
        1.0

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            val vowels = language.phonemeContainer.getPhonemes(PhonemeType.Vowel)
            val (firstVowel, secondVowel) = composeUniquePairs(vowels)
                .map { it.toSampleSpaceObject(calculateDistance(it.first, it.second).toDouble()) }
                .randomUnwrappedElementOrNull()
                ?: return listOf()

            0.5.chanceOf {
                return listOf(createRule("${escape(firstVowel)} -> ${escape(secondVowel)} / _ ") withProb 1.0)
            }

            return listOf(createRule("${escape(secondVowel)} -> ${escape(firstVowel)} / _ ") withProb 1.0)
        }
}
