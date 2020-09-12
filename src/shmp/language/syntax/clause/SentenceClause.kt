package shmp.language.syntax.clause

import shmp.language.Language
import shmp.language.syntax.clause.translation.SentenceClauseConstructor
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.clause.translation.SentenceType
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.orderer.RelationOrderer
import kotlin.random.Random


interface SentenceClause : UnfoldableClause {
    val type: SentenceType
}

abstract class AbstractSentenceClause(override val type: SentenceType) : SentenceClause {
    override fun unfold(language: Language, random: Random) =
        SentenceClauseConstructor(language.sentenceChangeParadigm)
            .applyNode(toNode(language, random), SyntaxRelation.Verb).second
}


class TransitiveVerbMainSentence(
    private val verbClause: TransitiveVerbClause
) : AbstractSentenceClause(SentenceType.MainClause) {
    override fun toNode(language: Language, random: Random): SentenceNode = verbClause.toNode(language, random)
        .copy(orderer = RelationOrderer(language.sentenceChangeParadigm.wordOrder.sovOrder.getValue(type), random))
}


class TransitiveVerbQuestion(
    private val verbClause: TransitiveVerbClause
) : AbstractSentenceClause(SentenceType.Question) {
    override fun toNode(language: Language, random: Random): SentenceNode = verbClause.toNode(language, random)
        .copy(orderer = RelationOrderer(language.sentenceChangeParadigm.wordOrder.sovOrder.getValue(type), random))
}
