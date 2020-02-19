package shmp.language

class PhonemeSequence(val phonemes: List<Phoneme>) {
    val size: Int = phonemes.size

    fun getRawString(): String = phonemes.joinToString("")

    operator fun get(i: Int): Phoneme = phonemes[i]

    fun last(): Phoneme = phonemes.last()

    fun getTypeRepresentation(): String = phonemes.joinToString("") { it.type.char.toString() }
}