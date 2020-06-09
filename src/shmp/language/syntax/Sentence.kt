package shmp.language.syntax

import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.lexis.Word
import shmp.language.category.Category
import shmp.language.category.CategorySource
import shmp.language.category.PersonValue
import shmp.language.category.paradigm.ParametrizedCategory
import shmp.language.category.paradigm.ParametrizedCategoryValue

data class Sentence(val node: SentenceNode)

data class SentenceNode(
    val word: Word,
    val categoryValues: List<CategoryValue>,
    val relation: MutableMap<SyntaxRelation, SentenceNode> = mutableMapOf()
) {
    fun extractValues(references: List<ParametrizedCategory>) = references.map { (category, source) ->
        val res = when(source) {
            is CategorySource.SelfStated -> categoryValues
            is CategorySource.RelationGranted -> relation[source.relation]?.categoryValues
        }?.firstOrNull { it.parentClassName == category.outType }
            ?: nullReferenceHandler(category, source)
            ?: throw LanguageException("No value for ${category.outType} and source $source")
        ParametrizedCategoryValue(res, source)
    }

    private fun nullReferenceHandler(category: Category, source: CategorySource): CategoryValue? {
        if (source == CategorySource.SelfStated) return null

        if (category.actualValues.contains(PersonValue.Third))
            return PersonValue.Third

        return null
    }
}
