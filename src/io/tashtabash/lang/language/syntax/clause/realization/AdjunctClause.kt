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

open class SimpleAdjunctClause(val word: Word, override val relation: SyntaxRelation) : AdjunctClause {
    override fun toNode(language: Language, random: Random): SyntaxNode =
        word.toNode(relation)
}

class AdverbClause(word: Word, relation: SyntaxRelation = SyntaxRelation.Manner) : SimpleAdjunctClause(word, relation) {
    init {
        if (word.semanticsCore.speechPart.type != SpeechPart.Adverb)
            throw SyntaxException("$word is not an adverb")
    }
}
