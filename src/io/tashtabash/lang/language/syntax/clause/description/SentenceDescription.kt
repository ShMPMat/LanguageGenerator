package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.*
import io.tashtabash.lang.language.syntax.clause.translation.CopulaSentenceType
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import kotlin.random.Random


abstract class SentenceDescription : UnfoldableClauseDescription


class TransitiveVerbMainClauseDescription(
    private val verbClause: TransitiveVerbDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        TransitiveVerbSentenceClause(
            verbClause.toClause(language, context, random),
            when (context.type.first) {
                Indicative -> VerbSentenceType.MainVerbClause
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
                Indicative -> VerbSentenceType.MainVerbClause
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
                Indicative -> CopulaSentenceType.MainCopulaClause
                GeneralQuestion -> CopulaSentenceType.QuestionCopulaClause
                Negative -> CopulaSentenceType.NegatedCopulaClause
            }
        )
}
