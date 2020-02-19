package shmp.language

data class Syllable(val phonemeSequence: PhonemeSequence) {
    constructor(phonemeList: List<Phoneme>) : this(PhonemeSequence(phonemeList))

    val size: Int = phonemeSequence.size

    override fun toString(): String {
        return phonemeSequence.getRawString()
    }

    operator fun get(i: Int): Phoneme = phonemeSequence[0]
}
