package shmp.lang.language.phonology

import shmp.lang.language.phonology.prosody.Prosody


data class Syllable(val phonemeSequence: PhonemeSequence, var prosodicEnums: List<Prosody>) {
    constructor(phonemeList: List<Phoneme>) : this(
        PhonemeSequence(phonemeList),
        listOf()
    )

    val size: Int = phonemeSequence.size

    override fun toString() = phonemeSequence.getRawString() + prosodicEnums.joinToString("") { it.mark }

    operator fun get(i: Int): Phoneme = phonemeSequence[0]
}
