package shmp.language.syntax.clause

import shmp.language.Language
import shmp.language.syntax.SentenceType


interface SentenceClause: SyntaxClause {
    val type: SentenceType
}

abstract class AbstractSentenceClause(override val type: SentenceType): SentenceClause


class TransitiveVerbMainSentence(
    private val verbClause: TransitiveVerbClause
): AbstractSentenceClause(SentenceType.MainClause) {
    override fun toNode(language: Language) = verbClause.toNode(language)
}


class TransitiveVerbQuestion(
    private val verbClause: TransitiveVerbClause
): AbstractSentenceClause(SentenceType.Question) {
    override fun toNode(language: Language) = verbClause.toNode(language)
}
