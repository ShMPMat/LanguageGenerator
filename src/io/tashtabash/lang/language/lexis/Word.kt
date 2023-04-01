package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.phonology.SyllableTemplate
import io.tashtabash.lang.language.phonology.Syllables
import io.tashtabash.lang.language.syntax.features.WordSyntaxRole


data class Word(
    val syllables: Syllables,
    val syllableTemplate: SyllableTemplate,
    val semanticsCore: SemanticsCore,
    val categoryValues: List<SourcedCategoryValue> = listOf(),
    val syntaxRole: WordSyntaxRole? = null
) {
    val size: Int = toPhonemes().size

    fun toPhonemes() = syllables.flatMap { it.phonemes.phonemes }

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
