package shmp.language.syntax

import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.Word
import shmp.language.category.Category
import shmp.language.category.CategorySource

data class Sentence(val node: SentenceNode)

data class SentenceNode(
    val word: Word,
    val categoryValues: List<CategoryValue>,
    val relation: Map<SyntaxRelation, SentenceNode>
) {
    fun extractValues(references: List<Pair<Category, CategorySource>>) = references.map { (category, source) ->
        when(source) {
            is CategorySource.SelfStated -> categoryValues
            is CategorySource.RelationGranted -> relation[source.relation]?.categoryValues
        }?.firstOrNull { it.parentClassName == category.outType }
            ?: throw LanguageException("No value for ${category.outType} and source $source in a node $this")
    }
}