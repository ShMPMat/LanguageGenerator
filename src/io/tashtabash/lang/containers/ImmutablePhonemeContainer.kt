package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType


data class ImmutablePhonemeContainer(override val phonemes: List<Phoneme>) : PhonemeContainer {
    override fun getPhonemes(phonemeType: PhonemeType): List<Phoneme> = phonemes.filter { it.type == phonemeType }

    override fun toString() = "$phonemes"
}
