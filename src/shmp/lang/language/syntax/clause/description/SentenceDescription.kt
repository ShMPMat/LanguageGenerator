package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.clause.realization.*
import shmp.lang.language.syntax.clause.translation.CopulaSentenceType
import shmp.lang.language.syntax.clause.translation.VerbSentenceType
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.context.ContextValue
import shmp.lang.language.syntax.context.ContextValue.TypeContext.*
import kotlin.random.Random


abstract class SentenceDescription : UnfoldableClauseDescription {
    abstract override fun toClause(language: Language, context: Context, random: Random): UnfoldableClause

    override fun unfold(language: Language, context: Context, random: Random): WordSequence =
        toClause(language, context, random).unfold(language, random)
}


class TransitiveVerbMainClauseDescription(
    private val verbClause: TransitiveVerbDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        TransitiveVerbSentenceClause(
            verbClause.toClause(language, context, random),
            when (context.type.first) {
                Simple -> VerbSentenceType.MainVerbClause
                GeneralQuestion -> VerbSentenceType.QuestionVerbClause
                Negative -> VerbSentenceType.NegatedVerbClause
            }
        )
}

class IntransitiveVerbMainClauseDescription(
    private val verbClause: IntransitiveVerbDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        IntransitiveVerbSentenceClause(
            verbClause.toClause(language, context, random),
            when (context.type.first) {
                Simple -> VerbSentenceType.MainVerbClause
                GeneralQuestion -> VerbSentenceType.QuestionVerbClause
                Negative -> VerbSentenceType.NegatedVerbClause
            }
        )
}


class CopulaMainClauseDescription(
    private val copulaClause: CopulaDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        CopulaSentenceClause(
            copulaClause.toClause(language, context, random),
            when (context.type.first) {
                Simple -> CopulaSentenceType.MainCopulaClause
                GeneralQuestion -> CopulaSentenceType.QuestionCopulaClause
                Negative -> CopulaSentenceType.NegatedCopulaClause
            }
        )
}
