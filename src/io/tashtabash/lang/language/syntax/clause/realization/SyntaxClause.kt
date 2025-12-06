package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.arranger.PassingArranger
import kotlin.random.Random


/**
 * The basic building block of the meta-language for describing grammar, which can be
 * transformed into a SentenceNode.
 */
interface SyntaxClause {
    fun toNode(language: Language, random: Random): SyntaxNode
}

interface UnfoldableClause: SyntaxClause {
    fun unfold(language: Language, random: Random): WordSequence
}


internal fun Word.toNode(
    generalType: SyntaxRelation,
    presetCategories: CategoryValues = listOf(),
    arranger: Arranger = PassingArranger
) = SyntaxNode(this, presetCategories.toMutableList(), arranger, generalType)
