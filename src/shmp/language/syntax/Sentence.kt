package shmp.language.syntax

import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.lexis.Word
import shmp.language.category.Category
import shmp.language.category.CategorySource
import shmp.language.category.PersonValue
import shmp.language.category.paradigm.ParametrizedCategory
import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.syntax.clause.SyntaxClause


data class Sentence(val syntaxClause: SyntaxClause, val type: SentenceType)


data class SentenceNode(
    val word: Word,
    val categoryValues: List<CategoryValue>,
    private val _relation: MutableMap<SyntaxRelation, SentenceNode> = mutableMapOf()
) {
    val relation: Map<SyntaxRelation, SentenceNode>
        get() = _relation

    fun setRelation(syntaxRelation: SyntaxRelation, sentenceNode: SentenceNode) {
        if (syntaxRelation == SyntaxRelation.Subject && word.semanticsCore.tags.any { it.name == "intrans" })
            throw SyntaxException("Cannot assign a direct subject to an intransitive verb $word")

        _relation[syntaxRelation] = sentenceNode
    }

    fun extractValues(references: List<ParametrizedCategory>) = references.map { (category, source) ->
        val res = when (source) {
            is CategorySource.SelfStated -> categoryValues
            is CategorySource.RelationGranted -> _relation[source.relation]?.categoryValues
        }?.firstOrNull { it.parentClassName == category.outType }
            ?: nullReferenceHandler(category, source)
            ?: throw LanguageException("No value for ${category.outType} and source $source")
        ParametrizedCategoryValue(res, source)
    }

    private fun nullReferenceHandler(category: Category, source: CategorySource): CategoryValue? {
        if (source == CategorySource.SelfStated)
            return null

        if (category.actualValues.contains(PersonValue.Third))
            return PersonValue.Third

        return null
    }
}


enum class SentenceType {
    MainClause,
    SubordinateClause,
    Question
}

fun differentWordOrderProbability(sentenceType: SentenceType) = when (sentenceType) {
    SentenceType.MainClause -> 0.0
    SentenceType.SubordinateClause -> 0.03
    SentenceType.Question -> 0.1
}
