package io.tashtabash.lang.language.syntax.clause.syntax

import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.clause.syntax.CopulaSentenceType.*
import io.tashtabash.lang.language.syntax.clause.syntax.VerbSentenceType.*


data class SyntaxNode(
    var word: Word,
    val categoryValues: MutableList<CategoryValue>, // Only its own values, excluding the values from the agreement
    var arranger: Arranger,
    var typeForChildren: SyntaxRelation,
    private val _relation: MutableMap<SyntaxRelation, SyntaxNode> = mutableMapOf(), // Inc. non-children relations
    private val _children: MutableList<SentenceNodeChild> = mutableListOf(),
    var isDropped: Boolean = false,
    var parentPropagation: Boolean = false
) {
    val children: List<SentenceNodeChild> = _children

    // Used to order the node and its children
    val allTreeRelations: List<SyntaxRelation>
        get() = listOf(typeForChildren) + children.map { it.first }

    fun setRelationChild(syntaxRelation: SyntaxRelation, child: SyntaxNode) {
        _relation[syntaxRelation] = child

        addStrayChild(syntaxRelation, child)
    }

    fun addStrayChild(syntaxRelation: SyntaxRelation, child: SyntaxNode) {
        _children += syntaxRelation to child

        child.setBackLink(this)
    }

    private fun setBackLink(syntaxNode: SyntaxNode, propagate: Boolean = parentPropagation) {
        _relation[syntaxNode.typeForChildren] = syntaxNode

        if (!propagate)
            return

        for (it in _children)
            it.second.setBackLink(syntaxNode, true)
    }

    /**
     * Extract CategoryValues using syntax relations (Agreement etc.)
     */
    fun extractValues(categories: List<SourcedCategory>): List<SourcedCategoryValue> =
        categories.mapNotNull { sourcedCategory ->
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
            sourcedCategory[res]
        }

    override fun toString() = "$word, $typeForChildren, $categoryValues"
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
    MainVerbClause -> .0
    SubordinateVerbClause -> .03
    QuestionVerbClause -> .1
    NegatedVerbClause -> .01
}

fun differentCopulaWordOrderProbability(sentenceType: CopulaSentenceType) = when (sentenceType) {
    // I haven't found any info about the probabilities
    MainCopulaClause -> .02
    SubordinateCopulaClause -> .02
    QuestionCopulaClause -> .02
    NegatedCopulaClause -> .01
}


typealias SentenceNodeChild = Pair<SyntaxRelation, SyntaxNode>
