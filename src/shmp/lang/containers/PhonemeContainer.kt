package shmp.lang.containers

import shmp.lang.language.phonology.Phoneme
import shmp.lang.language.PhonemeType

interface PhonemeContainer {
    val phonemes: List<Phoneme>

    fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme>
}