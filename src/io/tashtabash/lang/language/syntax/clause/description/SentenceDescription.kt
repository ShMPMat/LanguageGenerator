package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.clause.realization.*
import io.tashtabash.lang.language.syntax.clause.translation.CopulaSentenceType
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import kotlin.random.Random


abstract class SentenceDescription : UnfoldableClauseDescription


class VerbMainClauseDescription(private val verbClause: VerbDescription) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        verbClause.toClause(language, context, random).let {
            val type = when (context.type.first) {
                Indicative -> VerbSentenceType.MainVerbClause
                GeneralQuestion -> VerbSentenceType.QuestionVerbClause
                Negative -> VerbSentenceType.NegatedVerbClause
            }

            when (it) {
                is TransitiveVerbClause -> TransitiveVerbSentenceClause(it, type)
                is IntransitiveVerbClause -> IntransitiveVerbSentenceClause(it, type)
                else -> throw SyntaxException("Can't handle a clause of type '${it.javaClass}'")
            }
        }
}


class CopulaMainClauseDescription(private val copulaClause: CopulaDescription) : SentenceDescription() {
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
