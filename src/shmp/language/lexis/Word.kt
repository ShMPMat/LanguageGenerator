package shmp.language.lexis

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.phonology.Syllable
import shmp.language.phonology.SyllableTemplate
import shmp.language.syntax.features.WordSyntaxRole


data class Word(
    val syllables: List<Syllable>,
    val syllableTemplate: SyllableTemplate,
    val semanticsCore: SemanticsCore,
    val categoryValues: List<ParametrizedCategoryValue> = listOf(),
    val syntaxRole: WordSyntaxRole? = null
) {
    val size: Int = toPhonemes().size

    fun toPhonemes() = syllables.flatMap { it.phonemeSequence.phonemes }

    operator fun get(position: Int) = toPhonemes()[position]

    fun copyAndAddValues(values: Collection<ParametrizedCategoryValue>) =
        copy(categoryValues = categoryValues + values)

    fun copyWithValues(values: Collection<ParametrizedCategoryValue>) =
        copy(categoryValues = values.toList())

    override fun toString() = syllables.joinToString("")
}
