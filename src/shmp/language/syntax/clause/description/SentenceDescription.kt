package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.syntax.clause.realization.CopulaSentenceClause
import shmp.language.syntax.clause.realization.SyntaxClause
import shmp.language.syntax.clause.realization.TransitiveVerbSentenceClause
import shmp.language.syntax.clause.realization.UnfoldableClause
import shmp.language.syntax.clause.translation.SentenceType
import kotlin.random.Random


abstract class SentenceDescription: UnfoldableClauseDescription {
    abstract override fun toClause(language: Language, random: Random): UnfoldableClause

    override fun unfold(language: Language, random: Random) =
        toClause(language, random).unfold(language, random)
}


class TransitiveVerbMainClauseDescription(
    private val verbClause: TransitiveVerbDescription
) : SentenceDescription() {
    override fun toClause(language: Language, random: Random) = TransitiveVerbSentenceClause(
        verbClause.toClause(language, random),
        SentenceType.MainVerbClause
    )
}

class TransitiveVerbQuestionDescription(
    private val verbClause: TransitiveVerbDescription
) : SentenceDescription() {
    override fun toClause(language: Language, random: Random) = TransitiveVerbSentenceClause(
        verbClause.toClause(language, random),
        SentenceType.QuestionVerbClause
    )
}


class CopulaMainClauseDescription(
    private val copulaClause: CopulaDescription
) : SentenceDescription() {
    override fun toClause(language: Language, random: Random) = CopulaSentenceClause(
        copulaClause.toClause(language, random),
        SentenceType.MainCopulaClause
    )
}

class CopulaQuestionDescription(
    private val copulaClause: CopulaDescription
) : SentenceDescription() {
    override fun toClause(language: Language, random: Random) = CopulaSentenceClause(
        copulaClause.toClause(language, random),
        SentenceType.QuestionCopulaClause
    )
}
