package shmp.lang.language.phonology

import shmp.lang.language.phonology.prosody.Prosody


data class Syllable(val phonemeSequence: PhonemeSequence, var prosodicEnums: List<Prosody>) {
    constructor(phonemeList: List<Phoneme>) : this(PhonemeSequence(phonemeList), listOf())

    val size = phonemeSequence.size

    operator fun get(i: Int) = phonemeSequence[i]

    override fun toString() = phonemeSequence.getRawString() + prosodicEnums.joinToString("") { it.mark }
}


typealias Syllables = List<Syllable>
