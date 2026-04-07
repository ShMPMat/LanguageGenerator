package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.realization.CaseAdjunctClause
import io.tashtabash.lang.language.syntax.clause.realization.ObliquePredicatePossessionClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbClause
import io.tashtabash.lang.language.syntax.clause.syntax.VerbSentenceType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue
import io.tashtabash.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class PredicatePossessionDescription(
    val owner: NominalDescription,
    val owned: NominalDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.changeParadigm.syntaxParadigm.predicatePossession.predicatePossession
            .randomUnwrappedElement()
            .apply(owner, owned)
            .toClause(language, context, random)
}

class ObliquePredicatePossessionDescription(
    val owner: NominalDescription,
    val owned: NominalDescription,
    val possessorSyntaxRelation: SyntaxRelation
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        ObliquePredicatePossessionClause(
            language.lexis.getWord("exist").let { word ->
                VerbClause(
                    word,
                    language.changeParadigm.syntaxLogic.resolveVerbForm(
                        language,
                        word.semanticsCore.speechPart,
                        context
                    ),
                    mapOf(SyntaxRelation.Argument to owned.toClause(language, context, random)),
                    listOf(
                        CaseAdjunctClause(
                            owner.toClause(language, context, random),
                            possessorSyntaxRelation
                        )
                    )
                )
            },
            when (context.type.first) {
                ContextValue.TypeContext.Indicative -> VerbSentenceType.MainVerbClause
                ContextValue.TypeContext.GeneralQuestion -> VerbSentenceType.QuestionVerbClause
                ContextValue.TypeContext.Negative -> VerbSentenceType.NegatedVerbClause
            }
        )
}
