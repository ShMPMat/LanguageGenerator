package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType


interface PhonemeContainer {
    val phonemes: List<Phoneme>

    val size: Int
        get() = phonemes.size

    fun getPhoneme(symbol: String): Phoneme =
        phonemes.first { it.symbol == symbol }

    fun getPhonemeOrNull(symbol: String): Phoneme? =
        phonemes.firstOrNull { it.symbol == symbol }

    fun getPhonemes(phonemeType: PhonemeType): List<Phoneme> =
        phonemes.filter { it.type == phonemeType }

    fun getPhonemesNot(phonemeType: PhonemeType): List<Phoneme> =
        phonemes.filter { it.type != phonemeType }
}
