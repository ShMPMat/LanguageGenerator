package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.*
import io.tashtabash.lang.language.syntax.clause.syntax.CopulaSentenceType
import io.tashtabash.lang.language.syntax.clause.syntax.VerbSentenceType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import kotlin.random.Random


abstract class SentenceDescription : UnfoldableClauseDescription


class VerbMainClauseDescription(private val verb: VerbDescription) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        verb.toClause(language, context, random).let {
            val type = when (context.type.first) {
                Indicative -> VerbSentenceType.MainVerbClause
                GeneralQuestion -> VerbSentenceType.QuestionVerbClause
                Negative -> VerbSentenceType.NegatedVerbClause
            }

            val topic = context.topic?.let { objectType ->
                language.changeParadigm
                    .syntaxLogic
                    .resolveArgumentTypes(it.verb.semanticsCore.speechPart, objectType)
            }
            VerbSentenceClause(it, type, topic)
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
