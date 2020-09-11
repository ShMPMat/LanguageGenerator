package shmp.language.syntax.clause

import shmp.language.Language
import shmp.language.syntax.SentenceClauseConstructor
import shmp.language.syntax.SentenceType
import shmp.language.syntax.SyntaxRelation
import kotlin.random.Random


interface SentenceClause : UnfoldableClause {
    val type: SentenceType
}

abstract class AbstractSentenceClause(override val type: SentenceType) : SentenceClause {
    override fun unfold(language: Language, random: Random) =
        SentenceClauseConstructor(language.sentenceChangeParadigm, type, random)
            .applyNode(toNode(language), SyntaxRelation.Verb).second
}


class TransitiveVerbMainSentence(
    private val verbClause: TransitiveVerbClause
) : AbstractSentenceClause(SentenceType.MainClause) {
    override fun toNode(language: Language) = verbClause.toNode(language)
}


class TransitiveVerbQuestion(
    private val verbClause: TransitiveVerbClause
) : AbstractSentenceClause(SentenceType.Question) {
    override fun toNode(language: Language) = verbClause.toNode(language)
}
