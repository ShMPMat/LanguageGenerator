package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.syntax.clause.realization.TransitiveVerbSentenceClause
import shmp.language.syntax.clause.translation.SentenceType
import kotlin.random.Random


class TransitiveVerbMainClauseDescription(
    private val verbClause: TransitiveVerbDescription
) : ClauseDescription {
    override fun toClause(language: Language, random: Random) = TransitiveVerbSentenceClause(
        verbClause.toClause(language, random),
        SentenceType.MainClause
    )
}

class TransitiveVerbQuestionDescription(
    private val verbClause: TransitiveVerbDescription
) : ClauseDescription {
    override fun toClause(language: Language, random: Random) = TransitiveVerbSentenceClause(
        verbClause.toClause(language, random),
        SentenceType.Question
    )
}
