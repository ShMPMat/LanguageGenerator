package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.*
import io.tashtabash.lang.language.syntax.clause.syntax.CopulaSentenceType
import io.tashtabash.lang.language.syntax.clause.syntax.VerbSentenceType
import io.tashtabash.lang.language.syntax.context.DescriptionContext
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import kotlin.random.Random


abstract class SentenceDescription : UnfoldableClauseDescription


class VerbMainClauseDescription(private val verb: VerbDescription) : SentenceDescription() {
    override fun toClause(language: Language, context: DescriptionContext, random: Random) =
        verb.toClause(language, context, random).let { predicateClause ->
            val type = context.type.map {
                when (it.first) {
                    GeneralQuestion -> VerbSentenceType.QuestionVerbClause
                    Negative -> VerbSentenceType.NegatedVerbClause
                }
            }

            val topic = context.topic?.let { objectType ->
                language.changeParadigm
                    .syntaxLogic
                    .resolveArgumentTypes(predicateClause.head.semanticsCore.speechPart, objectType)
            }
            VerbSentenceClause(predicateClause, type, topic)
        }
}


class CopulaMainClauseDescription(private val copulaClause: CopulaDescription) : SentenceDescription() {
    override fun toClause(language: Language, context: DescriptionContext, random: Random) =
        CopulaSentenceClause(
            copulaClause.toClause(language, context, random),
            context.type.map {
                when (it.first) {
                    GeneralQuestion -> CopulaSentenceType.QuestionCopulaClause
                    Negative -> CopulaSentenceType.NegatedCopulaClause
                }
            }
        )
}
