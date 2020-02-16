package shmp.language

data class Syllable(val phonemes: List<Phoneme>) {
    val size: Int
    get() = phonemes.size

    override fun toString(): String {
        return phonemes.joinToString("") { it.toString() }
    }

    operator fun get(i: Int): Phoneme = phonemes[0]
}
