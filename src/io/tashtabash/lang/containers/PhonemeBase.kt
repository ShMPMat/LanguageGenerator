package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.*
import java.io.File


class PhonemeBase(supplementPath: String) : PhonemeContainer {
    override val phonemes: MutableList<Phoneme> = ArrayList()

    init {
        File("$supplementPath/Phonemes").forEachLine {
            if (!it.isBlank() && it[0] != '-') {
                val (sound, placeString, mannerString) = it.drop(1).split(" +".toRegex())
                val place = ArticulationPlace.valueOf(placeString)
                val manner = ArticulationManner.valueOf(mannerString)

                phonemes += Phoneme(sound, it[0].toPhonemeType(), place, manner)
            }
        }
    }

    override fun getPhonemesByType(phonemeType: PhonemeType) = phonemes.filter { it.type == phonemeType }
}
