package shmp.lang.containers

import shmp.lang.language.phonology.Phoneme
import shmp.lang.language.PhonemeType
import shmp.lang.language.phonology.ArticulationManner
import shmp.lang.language.phonology.ArticulationPlace
import shmp.lang.language.toPhonemeType
import java.io.File

class PhonemeBase(supplementPath: String) : PhonemeContainer {
    override val phonemes: MutableList<Phoneme> = ArrayList()

    init {
        File("$supplementPath/Phonemes").forEachLine {
            if (!it.isBlank() && it[0] != '-') {
                val tags = it.drop(1).split(" +".toRegex())
                phonemes.add(
                    Phoneme(
                    tags[0],
                    it[0].toPhonemeType(),
                    ArticulationPlace.valueOf(tags[1]),
                    ArticulationManner.valueOf(tags[2])
                )
                )
            }
        }
    }

    override fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme> = phonemes.filter { it.type == phonemeType }
}