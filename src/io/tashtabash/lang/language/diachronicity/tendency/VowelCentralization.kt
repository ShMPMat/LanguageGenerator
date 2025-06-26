package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRulesFor
import io.tashtabash.lang.language.phonology.ArticulationManner
import io.tashtabash.lang.language.phonology.ArticulationPlace
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.vowelArticulationPlaces
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.allWithProb
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.random.singleton.randomUnwrappedElementOrNull
import io.tashtabash.random.withProb
import kotlin.math.sign


class VowelCentralization : DefaultPhonologicalChangeTendency() {
    override fun getNewInstance() = VowelCentralization()

    override fun computeDevelopmentChance(language: Language): Double =
        1.0

    override fun getOptions(language: Language, phonemes: PhonemeContainer): List<GenericSSO<PhonologicalRule>> =
        phonemes.createPhonologicalRulesFor(language) {
            val nonCentralVowel = language.phonemeContainer.getPhonemes(PhonemeType.Vowel)
                .filter { it.articulationPlace != ArticulationPlace.Central || it.articulationManner != ArticulationManner.Mid }
                .randomElementOrNull()
                ?: return listOf()
            // Shift towards the center
            val centralizedArticulationPlacePosition = nonCentralVowel.articulationPlace.positionIndex +
                    2 * (ArticulationPlace.Central.positionIndex - nonCentralVowel.articulationPlace.positionIndex).sign
            val centralizedArticulationMannerPosition = nonCentralVowel.articulationManner.positionIndex +
                    (ArticulationManner.Mid.positionIndex - nonCentralVowel.articulationManner.positionIndex).sign
            val centralizedArticulationPlace = vowelArticulationPlaces
                .first { it.positionIndex == centralizedArticulationPlacePosition }
            val centralizedArticulationManner = ArticulationManner.entries
                .first { it.positionIndex == centralizedArticulationMannerPosition }
            val centralizedVowels = listOfNotNull(
                phonemes.getPhonemeByPropertiesOrNull(
                    nonCentralVowel.copy(articulationPlace = centralizedArticulationPlace)
                ),
                phonemes.getPhonemeByPropertiesOrNull(
                    nonCentralVowel.copy(articulationManner = centralizedArticulationManner)
                ),
                phonemes.getPhonemeByPropertiesOrNull(
                    nonCentralVowel.copy(articulationPlace = centralizedArticulationPlace, articulationManner = centralizedArticulationManner)
                )
            ) allWithProb 0.1
            val centralizedVowel = centralizedVowels.randomUnwrappedElementOrNull()
                ?: return listOf()

            return listOf(
                createRule("${escape(nonCentralVowel)} -> ${escape(centralizedVowel)} / _ ") withProb 0.1
            )
        }
}
