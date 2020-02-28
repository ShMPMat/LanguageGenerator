package shmp.language.phonology

import shmp.language.PhonemeType

data class Phoneme(val sound : String, val type: PhonemeType) {
    override fun toString(): String {
        return sound
    }
}

fun doesPhonemesCollide(firstPhoneme: Phoneme, secondPhoneme: Phoneme): Boolean {
    return firstPhoneme == secondPhoneme
}
