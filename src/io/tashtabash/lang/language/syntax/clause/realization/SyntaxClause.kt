package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.translation.SentenceNode
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.arranger.PassingArranger
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
