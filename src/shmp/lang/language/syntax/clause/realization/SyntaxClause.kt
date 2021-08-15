package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.category.value.CategoryValues
import shmp.lang.language.Language
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.lang.language.syntax.sequence.WordSequence
import shmp.lang.language.syntax.arranger.Arranger
import shmp.lang.language.syntax.arranger.PassingArranger
import kotlin.random.Random


interface SyntaxClause {
    fun toNode(language: Language, random: Random): SentenceNode
}

interface UnfoldableClause: SyntaxClause {
    fun unfold(language: Language, random: Random): WordSequence
}


internal fun Word.wordToNode(
    generalType: SyntaxRelation,
    arranger: Arranger = PassingArranger,
    presetCategories: CategoryValues = categoryValues.map { it.categoryValue }
) = SentenceNode(this, presetCategories.toMutableList(), arranger, generalType)
