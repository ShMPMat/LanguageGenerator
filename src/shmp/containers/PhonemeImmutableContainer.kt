package shmp.containers

import shmp.language.phonology.Phoneme
import shmp.language.PhonemeType

data class PhonemeImmutableContainer(override val phonemes: List<Phoneme>) : PhonemeContainer {
    override fun getPhonemesByType(phonemeType: PhonemeType): List<Phoneme> = phonemes.filter { it.type == phonemeType }

    override fun toString(): String {
        return "$phonemes"
    }
}