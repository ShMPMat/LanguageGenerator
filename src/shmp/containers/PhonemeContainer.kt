package shmp.containers

import shmp.language.Phoneme
import shmp.language.PhonemeType

interface PhonemeContainer {
    val phonemes: List<Phoneme>

    fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme>
}