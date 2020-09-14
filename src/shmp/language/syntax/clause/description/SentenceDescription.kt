package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.syntax.clause.realization.TransitiveVerbSentenceClause
import shmp.language.syntax.clause.translation.SentenceType
import kotlin.random.Random


class TransitiveVerbMainClauseDescription(
    private val verbClause: TransitiveVerbDescription
) : UnfoldableClauseDescription {
    override fun toClause(language: Language, random: Random) = TransitiveVerbSentenceClause(
        verbClause.toClause(language, random),
        SentenceType.MainClause
    )

    override fun unfold(language: Language, random: Random) =
        toClause(language, random).unfold(language, random)
}

class TransitiveVerbQuestionDescription(
    private val verbClause: TransitiveVerbDescription
) : UnfoldableClauseDescription {
    override fun toClause(language: Language, random: Random) = TransitiveVerbSentenceClause(
        verbClause.toClause(language, random),
        SentenceType.Question
    )

    override fun unfold(language: Language, random: Random) =
        toClause(language, random).unfold(language, random)
}
