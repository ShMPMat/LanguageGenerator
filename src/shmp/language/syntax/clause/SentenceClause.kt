package shmp.language.syntax.clause

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

abstract class AbstractSentenceClause(override val type: SentenceType) : SentenceClause {
    override fun unfold(language: Language, random: Random) =
        SentenceClauseConstructor(language.sentenceChangeParadigm)
            .applyNode(toNode(language.sentenceChangeParadigm, random), SyntaxRelation.Verb).second
}


class TransitiveVerbMainSentence(
    private val verbClause: TransitiveVerbClause
) : AbstractSentenceClause(SentenceType.MainClause) {
    override fun toNode(sentenceChangeParadigm: SentenceChangeParadigm, random: Random): SentenceNode =
        verbClause.toNode(sentenceChangeParadigm, random)
            .copy(orderer = RelationOrderer(sentenceChangeParadigm.wordOrder.sovOrder.getValue(type), random))
}


class TransitiveVerbQuestion(
    private val verbClause: TransitiveVerbClause
) : AbstractSentenceClause(SentenceType.Question) {
    override fun toNode(sentenceChangeParadigm: SentenceChangeParadigm, random: Random): SentenceNode =
        verbClause.toNode(sentenceChangeParadigm, random)
            .copy(orderer = RelationOrderer(sentenceChangeParadigm.wordOrder.sovOrder.getValue(type), random))
}
