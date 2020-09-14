package shmp.language.syntax.clause.realization

import shmp.language.Language
import shmp.language.syntax.SyntaxParadigm
import shmp.language.syntax.clause.translation.SentenceClauseConstructor
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.clause.translation.SentenceType
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.orderer.RelationOrderer
import kotlin.random.Random


interface SentenceClause : UnfoldableClause {
    val type: SentenceType
}


class TransitiveVerbSentenceClause(
    private val verbClause: TransitiveVerbClause,
    override val type: SentenceType
) : SentenceClause {
    override fun toNode(syntaxParadigm: SyntaxParadigm, random: Random): SentenceNode =
        verbClause.toNode(syntaxParadigm, random)
            .copy(orderer = RelationOrderer(syntaxParadigm.wordOrder.sovOrder.getValue(type), random))

    override fun unfold(language: Language, random: Random) =
        SentenceClauseConstructor(language.syntaxParadigm)
            .applyNode(toNode(language.syntaxParadigm, random), SyntaxRelation.Verb).second
}
