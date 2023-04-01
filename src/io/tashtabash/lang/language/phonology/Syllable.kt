package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.phonology.prosody.Prosody


data class Syllable(val phonemes: PhonemeSequence, var prosodicEnums: List<Prosody>) {
    constructor(phonemes: List<Phoneme>) : this(PhonemeSequence(phonemes), listOf())
    constructor(vararg phonemes: Phoneme) : this(phonemes.toList())

    val size = phonemes.size

    operator fun get(i: Int) = phonemes[i]

    fun last() = phonemes.last()

    override fun toString() = phonemes.getRawString() + prosodicEnums.joinToString("") { it.mark }
}


typealias Syllables = List<Syllable>
