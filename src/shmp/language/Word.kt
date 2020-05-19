package shmp.language

import shmp.language.phonology.Syllable
import shmp.language.phonology.SyllableTemplate

data class Word(val syllables: List<Syllable>, val syllableTemplate: SyllableTemplate, val syntaxCore: SyntaxCore) {
    val size: Int = toPhonemes().size

    fun toPhonemes() = syllables.flatMap { it.phonemeSequence.phonemes }

    override fun toString() = syllables.joinToString("")

    operator fun get(position: Int) = toPhonemes()[position]
}
