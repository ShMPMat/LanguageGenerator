package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.syntax.clause.realization.CopulaSentenceClause
import shmp.lang.language.syntax.clause.realization.TransitiveVerbSentenceClause
import shmp.lang.language.syntax.clause.realization.UnfoldableClause
import shmp.lang.language.syntax.clause.translation.CopulaSentenceType
import shmp.lang.language.syntax.clause.translation.VerbSentenceType
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
