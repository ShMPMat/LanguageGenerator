package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.realization.CaseAdjunctClause
import io.tashtabash.lang.language.syntax.clause.realization.IntransitiveVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.ObliquePredicatePossessionClause
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue
import io.tashtabash.lang.language.syntax.features.PredicatePossessionType.*
import io.tashtabash.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class PredicatePossessionDescription(
    val ownerDescription: NominalDescription,
    val ownedDescription: NominalDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        when (language.changeParadigm.syntaxParadigm.predicatePossessionPresence.predicatePossessionType.randomUnwrappedElement()) {
            HaveVerb ->
                VerbMainClauseDescription(
                    VerbDescription(
                        "have",
                        mapOf(
                            MainObjectType.Agent to ownerDescription,
                            MainObjectType.Patient to ownedDescription
                        )
                    )
                )
            LocativeOblique -> ObliquePredicatePossessionDescription(
                ownerDescription,
                ownedDescription,
                SyntaxRelation.Location
            )
            DativeOblique -> ObliquePredicatePossessionDescription(
                ownerDescription,
                ownedDescription,
                SyntaxRelation.Addressee
            )
            GenitiveOblique -> VerbMainClauseDescription(
                VerbDescription(
                    "exist",
                    mapOf(
                        MainObjectType.Argument to ownedDescription.copyAndAddDefinitions(PossessorDescription(ownerDescription))
                    )
                )
            )
            Topic -> ObliquePredicatePossessionDescription(
                ownerDescription,
                ownedDescription,
                SyntaxRelation.Topic
            )
        }.toClause(language, context, random)
}

class ObliquePredicatePossessionDescription(
    val ownerDescription: NominalDescription,
    val ownedDescription: NominalDescription,
    val possessorSyntaxRelation: SyntaxRelation
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        ObliquePredicatePossessionClause(
            language.lexis.getWordOrNull("exist")?.let { word ->
                IntransitiveVerbClause(
                    word,
                    language.changeParadigm.syntaxLogic.resolveVerbForm(
                        language,
                        word.semanticsCore.speechPart,
                        context
                    ),
                    ownedDescription.toClause(language, context, random),
                    listOf(
                        CaseAdjunctClause(
                            ownerDescription.toClause(language, context, random),
                            possessorSyntaxRelation
                        )
                    )
                )
            }
                ?: throw SyntaxException("No verb 'exist' in Language"),
            when (context.type.first) {
                ContextValue.TypeContext.Indicative -> VerbSentenceType.MainVerbClause
                ContextValue.TypeContext.GeneralQuestion -> VerbSentenceType.QuestionVerbClause
                ContextValue.TypeContext.Negative -> VerbSentenceType.NegatedVerbClause
            }
        )
}
