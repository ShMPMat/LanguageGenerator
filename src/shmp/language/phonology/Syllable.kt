package shmp.language.phonology

import shmp.language.phonology.Phoneme
import shmp.language.phonology.PhonemeSequence

data class Syllable(val phonemeSequence: PhonemeSequence) {
    constructor(phonemeList: List<Phoneme>) : this(
        PhonemeSequence(
            phonemeList
        )
    )

    val size: Int = phonemeSequence.size

    override fun toString(): String {
        return phonemeSequence.getRawString()
    }

    operator fun get(i: Int): Phoneme = phonemeSequence[0]
}
