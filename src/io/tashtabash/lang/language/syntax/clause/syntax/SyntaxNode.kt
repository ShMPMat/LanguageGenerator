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


data class SyntaxNode(
    var word: Word,
    val categoryValues: MutableList<CategoryValue>, // Only its own values, excluding the values from the agreement
    var arranger: Arranger,
    var typeForChildren: SyntaxRelation,
    private val _relation: MutableMap<SyntaxRelation, SyntaxNode> = mutableMapOf(), // Inc. non-children relations
    private val _children: MutableList<SentenceNodeChild> = mutableListOf(),
    var isDropped: Boolean = false,
    var parentPropagation: Boolean = false,
    val tags: MutableList<SyntaxNodeTag> = mutableListOf()
) {
    val children: List<SentenceNodeChild> = _children
    val relations: Map<SyntaxRelation, SyntaxNode> = _relation

    // Used to order the node and its children
    val allTreeRelations: List<SyntaxRelation>
        get() = listOf(typeForChildren) + children.map { it.first }

    fun setRelationChild(syntaxRelation: SyntaxRelation, child: SyntaxNode, backType: SyntaxRelation = typeForChildren) {
        _relation[syntaxRelation] = child

        addStrayChild(syntaxRelation, child, backType)
    }

    fun addStrayChild(syntaxRelation: SyntaxRelation, child: SyntaxNode, backType: SyntaxRelation = typeForChildren) {
        _children += syntaxRelation to child

        child.setBackLink(this, backType)
    }

    private fun setBackLink(syntaxNode: SyntaxNode, backType: SyntaxRelation, propagate: Boolean = parentPropagation) {
        _relation[backType] = syntaxNode

        if (!propagate)
            return

        for (it in _children)
            it.second.setBackLink(syntaxNode, backType, true)
    }

    /**
     * Extract CategoryValues using syntax relations (Agreement etc.)
     */
    fun extractValues(categories: List<SourcedCategory>): List<SourcedCategoryValue> =
        categories.mapNotNull { sourcedCategory ->
            val (category, source, compulsoryData) = sourcedCategory

            val res = when (source) {
                is CategorySource.Self -> categoryValues + word.semanticsCore.staticCategories
                is CategorySource.Agreement -> extractAgreementValues(source.relation)
            }
                ?.firstOrNull { it.parentClassName == category.outType }
                ?: run {
                    if (compulsoryData.mustExist(categoryValues))
                        throw SyntaxException(
                            "$word: No value for compulsory category ${category.outType} and source $source"
                        )
                    else
                        return@mapNotNull null
                }
            sourcedCategory[res]
        }

    private fun extractAgreementValues(relations: List<SyntaxRelation>): List<CategoryValue>? =
        relations.firstNotNullOfOrNull { relation ->
            _relation[relation]
                ?.let { it.categoryValues + it.word.semanticsCore.staticCategories }
                // A work-around for Aux getting arguments from the governed verb
                ?: _relation[SyntaxRelation.Predicate]?.extractAgreementValues(relations)
        }

    override fun toString() = "$word, $typeForChildren, $categoryValues"
}


interface SentenceType

enum class VerbSentenceType : SentenceType {
    MainVerbClause,
    QuestionVerbClause,
    NegatedVerbClause
}

enum class CopulaSentenceType : SentenceType {
    MainCopulaClause,
    SubordinateCopulaClause,
    QuestionCopulaClause,
    NegatedCopulaClause
}

fun differentCopulaWordOrderProbability(sentenceType: CopulaSentenceType) = when (sentenceType) {
    // I haven't found any info about the probabilities
    MainCopulaClause -> .02
    SubordinateCopulaClause -> .02
    QuestionCopulaClause -> .02
    NegatedCopulaClause -> .01
}


typealias SentenceNodeChild = Pair<SyntaxRelation, SyntaxNode>

enum class SyntaxNodeTag {
    Topic,
    Question
}
