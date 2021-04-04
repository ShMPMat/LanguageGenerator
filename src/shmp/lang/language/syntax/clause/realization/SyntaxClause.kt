package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.CategoryValue
import shmp.lang.language.Language
import shmp.lang.language.category.CaseValue
import shmp.lang.language.category.CategorySource
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.arranger.Arranger
import kotlin.random.Random


interface SyntaxClause {
    fun toNode(language: Language, random: Random): SentenceNode
}

interface UnfoldableClause: SyntaxClause {
    fun unfold(language: Language, random: Random): WordSequence
}


internal fun Word.wordToNode(
    changeParadigm: ChangeParadigm,
    arranger: Arranger,
    generalType: SyntaxRelation,
    presetCategories: List<CategoryValue> = this.categoryValues.map { it.categoryValue }
): SentenceNode {
    val classNames = presetCategories
        .map { it.parentClassName }

    val selfStatedAbsentValues = changeParadigm.wordChangeParadigm
        .getDefaultState(this)
        .filter { it.source == CategorySource.SelfStated }
        .map { it.categoryValue }
        .filter { it.parentClassName !in classNames }//TODO a crutch; make categories compulsory and otherwise
//        .filter { it !is CaseValue }

    if (selfStatedAbsentValues.isNotEmpty()) {
        val k = 0
    }

    return SentenceNode(
        this,
        selfStatedAbsentValues + presetCategories,
        arranger,
        generalType
    )
}
