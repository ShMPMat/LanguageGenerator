package shmp.lang.language.syntax.clause.translation

import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategorySource
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.arranger.Arranger
import shmp.lang.language.syntax.clause.translation.CopulaSentenceType.*
import shmp.lang.language.syntax.clause.translation.VerbSentenceType.*


class SentenceNode(
    val word: Word,
    categoryValues: List<CategoryValue>,
    var arranger: Arranger,
    var typeForChildren: SyntaxRelation,
    private val _relation: MutableMap<SyntaxRelation, SentenceNode> = mutableMapOf(),
    private val _children: MutableList<SentenceNodeChild> = mutableListOf(),
    var isDropped: Boolean = false
) {
    private val _categoryValues = categoryValues.toMutableList()
    val categoryValues: MutableList<CategoryValue>
        get() = _categoryValues

    val children: List<SentenceNodeChild>
        get() = _children

    fun insertCategoryValue(value: CategoryValue) {
        _categoryValues += listOf(value)
    }

    fun setRelationChild(syntaxRelation: SyntaxRelation, child: SentenceNode) {
        _relation[syntaxRelation] = child

        addStrayChild(syntaxRelation, child)
    }

    fun addStrayChild(syntaxRelation: SyntaxRelation, child: SentenceNode) {
        _children.add(syntaxRelation to child)

        child.setBackLink(this)
    }

    private fun setBackLink(sentenceNode: SentenceNode) {
        _relation[sentenceNode.typeForChildren] = sentenceNode
    }

    fun extractValues(references: List<SourcedCategory>) =
        references.mapNotNull { (category, source, isCompulsory) ->
            val res = when (source) {
                is CategorySource.SelfStated -> categoryValues
                is CategorySource.RelationGranted -> _relation[source.relation]?.categoryValues
            }
                ?.firstOrNull { it.parentClassName == category.outType }
                ?: run {
                    if (isCompulsory)
                        throw SyntaxException("No value for compulsory category ${category.outType} and source $source")
                    else
                        return@mapNotNull null
                }
            SourcedCategoryValue(res, source)
        }
}


interface SentenceType

enum class VerbSentenceType : SentenceType {
    MainVerbClause,
    SubordinateVerbClause,
    QuestionVerbClause
}

enum class CopulaSentenceType : SentenceType {
    MainCopulaClause,
    SubordinateCopulaClause,
    QuestionCopulaClause
}


fun differentWordOrderProbability(sentenceType: VerbSentenceType) = when (sentenceType) {
    MainVerbClause -> 0.0
    SubordinateVerbClause -> 0.03
    QuestionVerbClause -> 0.1
}

fun differentCopulaWordOrderProbability(sentenceType: CopulaSentenceType) = when (sentenceType) {
    //TODO no idea about these probabilities here
    MainCopulaClause -> 0.02
    SubordinateCopulaClause -> 0.02
    QuestionCopulaClause -> 0.02
}


typealias SentenceNodeChild = Pair<SyntaxRelation, SentenceNode>
