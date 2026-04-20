package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.transformer.AddCategoryTransformer
import kotlin.random.Random


interface AdjunctClause : SyntaxClause {
    val relation: SyntaxRelation
}

class CaseAdjunctClause(
    val nominal: NominalClause,
    override val relation: SyntaxRelation
) : AdjunctClause {
    override fun toNode(language: Language, random: Random): SyntaxNode =
        nominal.toNode(language, random)
            .also { AddCategoryTransformer(relation).apply(it, language.changeParadigm.syntaxLogic) }
}

class AdverbClause(
    val adverb: Word,
    override val relation: SyntaxRelation = SyntaxRelation.Manner
) : AdjunctClause {
    init {
        if (adverb.semanticsCore.speechPart.type != SpeechPart.Adverb)
            throw SyntaxException("$adverb is not an adverb")
    }

    override fun toNode(language: Language, random: Random): SyntaxNode =
        adverb.toNode(relation)
}
