package shmp.language

import shmp.generator.SyllableTemplate

data class Word(val syllables: List<Syllable>, val syllableTemplate: SyllableTemplate, val syntaxCore: SyntaxCore) {
    val size: Int = toPhonemes().size

    fun addPhonemes(phonemes: List<Phoneme>) {
        //TODO
    }

    fun toPhonemes(): List<Phoneme> {
        return syllables.flatMap { it.phonemeSequence.phonemes }
    }

    override fun toString(): String {
        return syllables.joinToString("") + " - " + syntaxCore.word
    }

    operator fun get(position: Int) = toPhonemes()[position]
}