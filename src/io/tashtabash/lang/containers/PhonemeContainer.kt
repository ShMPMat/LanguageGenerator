package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType

interface PhonemeContainer {
    val phonemes: List<Phoneme>

    fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme>
}