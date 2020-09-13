package shmp.language.syntax.clause.realization

import shmp.language.Language
import shmp.language.category.paradigm.SentenceChangeParadigm
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
    override fun toNode(sentenceChangeParadigm: SentenceChangeParadigm, random: Random): SentenceNode =
        verbClause.toNode(sentenceChangeParadigm, random)
            .copy(orderer = RelationOrderer(sentenceChangeParadigm.wordOrder.sovOrder.getValue(type), random))

    override fun unfold(language: Language, random: Random) =
        SentenceClauseConstructor(language.sentenceChangeParadigm)
            .applyNode(toNode(language.sentenceChangeParadigm, random), SyntaxRelation.Verb).second
}
