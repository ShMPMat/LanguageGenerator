package shmp.lang.language.syntax.clause.translation

import shmp.lang.language.CategoryValue
import shmp.lang.language.CategoryValues
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
        _categoryValues.add(value)
    }

    fun insertCategoryValues(values: CategoryValues) {
        _categoryValues.addAll(values)
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
        references.mapNotNull { sourcedCategory ->
            val (category, source, compulsoryData) = sourcedCategory

            val res = when (source) {
                is CategorySource.SelfStated -> categoryValues + word.semanticsCore.staticCategories
                is CategorySource.RelationGranted -> _relation[source.relation]?.let { it.categoryValues + it.word.semanticsCore.staticCategories }
            }
                ?.firstOrNull { it.parentClassName == category.outType }
                ?: run {
                    val hasAllCategoryClusters = compulsoryData.isApplicable(categoryValues)

                    if (compulsoryData.isCompulsory && hasAllCategoryClusters)
                        throw SyntaxException("No value for compulsory category ${category.outType} and source $source")
                    else
                        return@mapNotNull null
                }
            SourcedCategoryValue(res, source, sourcedCategory)
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
