package shmp.containers

import shmp.language.phonology.Phoneme
import shmp.language.PhonemeType
import shmp.language.phonology.ArticulationManner
import shmp.language.phonology.ArticulationPlace
import shmp.language.toPhonemeType
import java.io.File

class PhonemeBase(supplementPath: String) : PhonemeContainer {
    override val phonemes: MutableList<Phoneme> = ArrayList()

    init {
        File("$supplementPath/Phonemes").forEachLine {
            if (!it.isBlank()) {
                val tags = it.drop(1).split(" +".toRegex())
                phonemes.add(Phoneme(
                    tags[0],
                    it[0].toPhonemeType(),
                    ArticulationPlace.valueOf(tags[1]),
                    ArticulationManner.valueOf(tags[2])
                ))
            }
        }
    }

    override fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme> = phonemes.filter { it.type == phonemeType }
}