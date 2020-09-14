package shmp.language.syntax.clause.realization

import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.category.CategorySource
import shmp.language.syntax.SyntaxParadigm
import shmp.language.lexis.Word
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.WordSequence
import shmp.language.syntax.orderer.Orderer
import kotlin.random.Random


interface SyntaxClause {
    fun toNode(syntaxParadigm: SyntaxParadigm, random: Random): SentenceNode
}

interface UnfoldableClause: SyntaxClause {
    fun unfold(language: Language, random: Random): WordSequence
}


internal fun Word.wordToNode(
    syntaxParadigm: SyntaxParadigm,
    orderer: Orderer,
    presetCategories: List<CategoryValue> = listOf()
): SentenceNode {
    val classNames = presetCategories
        .map { it.parentClassName }

    return SentenceNode(
        this,
        syntaxParadigm.wordChangeParadigm
            .getDefaultState(this)
            .filter { it.source == CategorySource.SelfStated }
            .map { it.categoryValue }
            .filter { it.parentClassName !in classNames }
                + presetCategories,
        orderer
    )
}