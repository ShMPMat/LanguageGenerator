package shmp.language

import shmp.generator.SyllableTemplate
import shmp.language.phonology.Phoneme
import shmp.language.phonology.Syllable

data class Word(val syllables: List<Syllable>, val syllableTemplate: SyllableTemplate, val syntaxCore: SyntaxCore) {
    val size: Int = toPhonemes().size

    fun toPhonemes(): List<Phoneme> {
        return syllables.flatMap { it.phonemeSequence.phonemes }
    }

    override fun toString(): String {
        return syllables.joinToString("")
    }

    operator fun get(position: Int) = toPhonemes()[position]
}