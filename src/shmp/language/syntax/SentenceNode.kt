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
import shmp.language.syntax.orderer.Orderer


data class SentenceNode(
    val word: Word,
    val categoryValues: List<CategoryValue>,
    val orderer: Orderer,
    private val _relation: MutableMap<SyntaxRelation, SentenceNode> = mutableMapOf(),
    private val _children: MutableList<SentenceNodeChild> = mutableListOf()
) {
    val children: List<SentenceNodeChild>
        get() = _children

    fun withCategoryValue(value: CategoryValue) = this.copy(
        categoryValues = categoryValues + listOf(value)
    )

    fun setRelation(syntaxRelation: SyntaxRelation, sentenceNode: SentenceNode, isChild: Boolean) {
        _relation[syntaxRelation] = sentenceNode

        if (isChild)
            addChild(syntaxRelation, sentenceNode)
    }

    fun addChild(syntaxRelation: SyntaxRelation, sentenceNode: SentenceNode) =
        _children.add(syntaxRelation to sentenceNode)

    fun extractValues(references: List<ParametrizedCategory>) =
        references.map { (category, source) ->
            val res = when (source) {
                is CategorySource.SelfStated -> categoryValues
                is CategorySource.RelationGranted -> _relation[source.relation]?.categoryValues
            }
                ?.firstOrNull { it.parentClassName == category.outType }
                ?: throw LanguageException("No value for ${category.outType} and source $source")
            ParametrizedCategoryValue(res, source)
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


typealias SentenceNodeChild = Pair<SyntaxRelation, SentenceNode>
