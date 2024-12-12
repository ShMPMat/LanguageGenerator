package io.tashtabash.lang.language.phonology


data class PhonemeSequence(val phonemes: List<Phoneme>) {
    constructor(vararg phoneme: Phoneme) : this(phoneme.toList())

    val size: Int =
        phonemes.size

    operator fun get(i: Int): Phoneme =
        phonemes[i]

    fun last(): Phoneme =
        phonemes.last()

    fun reversed(): PhonemeSequence =
        PhonemeSequence(phonemes.reversed())

    fun getTypeRepresentation(): String =
        phonemes.joinToString("") { it.type.char.toString() }

    override fun toString() =
        phonemes.joinToString("")
}
