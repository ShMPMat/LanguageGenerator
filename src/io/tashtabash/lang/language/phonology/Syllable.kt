package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.phonology.prosody.Prosody


data class Syllable(val phonemes: PhonemeSequence, val prosody: List<Prosody>, val nucleusIdx: Int) {
    constructor(phonemes: List<Phoneme>, nucleusIdx: Int) : this(PhonemeSequence(phonemes), listOf(), nucleusIdx)
    constructor(nucleusIdx: Int, vararg phonemes: Phoneme) : this(phonemes.toList(), nucleusIdx)

    val size = phonemes.size

    operator fun get(i: Int) = phonemes[i]

    fun last() = phonemes.last()

    override fun toString() =
        phonemes.phonemes.take(nucleusIdx + 1).joinToString("") +
                prosody.joinToString("") { it.mark } +
                phonemes.phonemes.subList(nucleusIdx + 1, phonemes.size).joinToString("")

}


typealias Syllables = List<Syllable>
