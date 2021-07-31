package shmp.lang.language.lexis

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.phonology.Syllable
import shmp.lang.language.phonology.SyllableTemplate
import shmp.lang.language.syntax.features.WordSyntaxRole


data class Word(
    val syllables: List<Syllable>,
    val syllableTemplate: SyllableTemplate,
    val semanticsCore: SemanticsCore,
    val categoryValues: List<SourcedCategoryValue> = listOf(),
    val syntaxRole: WordSyntaxRole? = null
) {
    val size: Int = toPhonemes().size

    fun toPhonemes() = syllables.flatMap { it.phonemeSequence.phonemes }

    operator fun get(position: Int) = toPhonemes()[position]

    fun copyAndAddValues(values: Collection<SourcedCategoryValue>) =
        copy(categoryValues = categoryValues + values)

    fun copyWithValues(values: Collection<SourcedCategoryValue>) =
        copy(categoryValues = values.toList())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Word

        if (syllables != other.syllables) return false
        if (semanticsCore != other.semanticsCore) return false

        return true
    }

    override fun hashCode(): Int {
        var result = syllables.hashCode()
        result = 31 * result + semanticsCore.hashCode()
        return result
    }

    override fun toString() = syllables.joinToString("")
}
