package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.*
import java.io.File


class PhonemePool(supplementPath: String) : PhonemeContainer {
    override val phonemes: MutableList<Phoneme> = ArrayList()

    init {
        File("$supplementPath/Phonemes").forEachLine { line ->
            if (line.isBlank() || line[0] == '-') {
                return@forEachLine
            }
            val featureStrings = line.drop(1).split(" +".toRegex())
            val (sound, placeString, mannerString) = featureStrings
            val modifierStrings = featureStrings.drop(3)

            val place = ArticulationPlace.valueOf(placeString)
            val manner = ArticulationManner.valueOf(mannerString)
            val modifiers = modifierStrings.map { PhonemeModifier.valueOf(it) }

            phonemes += Phoneme(sound, line[0].toPhonemeType(), place, manner, modifiers)
        }
    }

    override fun getPhonemes(phonemeType: PhonemeType) = phonemes.filter { it.type == phonemeType }
}
