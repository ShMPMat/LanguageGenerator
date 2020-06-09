package shmp.language

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.phonology.Syllable
import shmp.language.phonology.SyllableTemplate

data class Word(
    val syllables: List<Syllable>,
    val syllableTemplate: SyllableTemplate,
    val semanticsCore: SemanticsCore,
    val categoryValues: List<ParametrizedCategoryValue> = listOf()
) {
    val size: Int = toPhonemes().size

    fun toPhonemes() = syllables.flatMap { it.phonemeSequence.phonemes }

    override fun toString() = syllables.joinToString("")

    operator fun get(position: Int) = toPhonemes()[position]
}
