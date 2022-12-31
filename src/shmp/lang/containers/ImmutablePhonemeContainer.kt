package shmp.lang.containers

import shmp.lang.language.phonology.Phoneme
import shmp.lang.language.phonology.PhonemeType

data class ImmutablePhonemeContainer(override val phonemes: List<Phoneme>) : PhonemeContainer {
    override fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme> = phonemes.filter { it.type == phonemeType }

    override fun toString(): String {
        return "$phonemes"
    }
}