package io.tashtabash.lang.language.syntax.clause.translation

import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.clause.translation.CopulaSentenceType.*
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType.*


data class SentenceNode(
    val word: Word,
    private val _categoryValues: MutableList<CategoryValue>,
    var arranger: Arranger,
    var typeForChildren: SyntaxRelation,
    private val _relation: MutableMap<SyntaxRelation, SentenceNode> = mutableMapOf(),
    private val _children: MutableList<SentenceNodeChild> = mutableListOf(),
    var isDropped: Boolean = false,
    var parentPropagation: Boolean = false,
    var nodesOrder: List<SyntaxRelation> = listOf()
) {
    val categoryValues: MutableList<CategoryValue>
        get() = _categoryValues

    val children: List<SentenceNodeChild>
        get() = _children

    val allRelations: List<SyntaxRelation>
        get() = listOf(typeForChildren) + children.map { it.first }

    fun addCategoryValue(value: CategoryValue) {
        _categoryValues += value
    }

    fun addCategoryValues(values: CategoryValues) {
        _categoryValues += values
    }

    fun setRelationChild(syntaxRelation: SyntaxRelation, child: SentenceNode) {
        _relation[syntaxRelation] = child

        addStrayChild(syntaxRelation, child)
    }

    fun addStrayChild(syntaxRelation: SyntaxRelation, child: SentenceNode) {
        _children += syntaxRelation to child

        child.setBackLink(this)
    }

    private fun setBackLink(sentenceNode: SentenceNode, propagate: Boolean = parentPropagation) {
        _relation[sentenceNode.typeForChildren] = sentenceNode

        if (propagate)
            _children.forEach {
                it.second.setBackLink(sentenceNode, true)
            }
    }

    /**
     * Extract CategoryValues using syntax relations (Agreement etc.)
     */
    fun extractValues(references: List<SourcedCategory>): List<SourcedCategoryValue> =
        references.mapNotNull { sourcedCategory ->
            val (category, source, compulsoryData) = sourcedCategory

            val res = when (source) {
                is CategorySource.Self -> categoryValues + word.semanticsCore.staticCategories
                is CategorySource.Agreement -> _relation[source.relation]
                    ?.let { it.categoryValues + it.word.semanticsCore.staticCategories }
            }
                ?.firstOrNull { it.parentClassName == category.outType }
                ?: run {
                    val hasAllCategoryClusters = compulsoryData.isApplicable(categoryValues)

                    if (compulsoryData.isCompulsory && hasAllCategoryClusters)
                        throw SyntaxException("No value for compulsory category ${category.outType} and source $source")
                    else
                        return@mapNotNull null
                }
            sourcedCategory.getValue(res)
        }
}


interface SentenceType

enum class VerbSentenceType : SentenceType {
    MainVerbClause,
    SubordinateVerbClause,
    QuestionVerbClause,
    NegatedVerbClause
}

enum class CopulaSentenceType : SentenceType {
    MainCopulaClause,
    SubordinateCopulaClause,
    QuestionCopulaClause,
    NegatedCopulaClause
}


fun differentWordOrderProbability(sentenceType: VerbSentenceType) = when (sentenceType) {
    MainVerbClause -> 0.0
    SubordinateVerbClause -> 0.03
    QuestionVerbClause -> 0.1
    NegatedVerbClause -> 0.01
}

fun differentCopulaWordOrderProbability(sentenceType: CopulaSentenceType) = when (sentenceType) {
    //TODO no idea about these probabilities here
    MainCopulaClause -> 0.02
    SubordinateCopulaClause -> 0.02
    QuestionCopulaClause -> 0.02
    NegatedCopulaClause -> 0.01
}


typealias SentenceNodeChild = Pair<SyntaxRelation, SentenceNode>
