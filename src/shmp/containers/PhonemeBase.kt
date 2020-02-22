package shmp.containers

import shmp.language.phonology.Phoneme
import shmp.language.PhonemeType
import shmp.language.toPhonemeType
import java.io.File

class PhonemeBase : PhonemeContainer {
    override val phonemes: MutableList<Phoneme> = ArrayList()

    init {
        File("SupplementFiles/Phonemes").forEachLine {
            if (!it.isBlank()) {
                phonemes.add(Phoneme(it.substring(1), it[0].toPhonemeType()))
            }
        }
    }

    override fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme> = phonemes.filter { it.type == phonemeType }
}