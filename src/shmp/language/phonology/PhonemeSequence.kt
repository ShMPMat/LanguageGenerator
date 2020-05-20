package shmp.language.phonology

data class PhonemeSequence(val phonemes: List<Phoneme>) {
    constructor(vararg phoneme: Phoneme) : this(phoneme.toList())

    val size: Int = phonemes.size

    fun getRawString(): String = phonemes.joinToString("")

    operator fun get(i: Int): Phoneme = phonemes[i]

    fun last(): Phoneme = phonemes.last()

    fun getTypeRepresentation(): String = phonemes.joinToString("") { it.type.char.toString() }
}