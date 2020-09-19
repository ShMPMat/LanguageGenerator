package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.syntax.clause.realization.CopulaSentenceClause
import shmp.language.syntax.clause.realization.TransitiveVerbSentenceClause
import shmp.language.syntax.clause.realization.UnfoldableClause
import shmp.language.syntax.clause.translation.CopulaSentenceType
import shmp.language.syntax.clause.translation.VerbSentenceType
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
        VerbSentenceType.MainVerbClause
    )
}

class TransitiveVerbQuestionDescription(
    private val verbClause: TransitiveVerbDescription
) : SentenceDescription() {
    override fun toClause(language: Language, random: Random) = TransitiveVerbSentenceClause(
        verbClause.toClause(language, random),
        VerbSentenceType.QuestionVerbClause
    )
}


class CopulaMainClauseDescription(
    private val copulaClause: CopulaDescription
) : SentenceDescription() {
    override fun toClause(language: Language, random: Random) = CopulaSentenceClause(
        copulaClause.toClause(language, random),
        CopulaSentenceType.MainCopulaClause
    )
}

class CopulaQuestionDescription(
    private val copulaClause: CopulaDescription
) : SentenceDescription() {
    override fun toClause(language: Language, random: Random) = CopulaSentenceClause(
        copulaClause.toClause(language, random),
        CopulaSentenceType.QuestionCopulaClause
    )
}
