package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.category.CaseValue
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.realization.CaseAdjunctClause
import shmp.lang.language.syntax.clause.realization.IntransitiveVerbClause
import shmp.lang.language.syntax.clause.realization.ObliquePredicatePossessionClause
import shmp.lang.language.syntax.clause.translation.VerbSentenceType
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.context.ContextValue
import shmp.lang.language.syntax.features.PredicatePossessionType.*
import shmp.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class PredicatePossessionDescription(
    val ownerDescription: NominalDescription,
    val ownedDescription: NominalDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        when (language.changeParadigm.syntaxParadigm.predicatePossessionPresence.predicatePossessionType.randomUnwrappedElement()) {
            HaveVerb ->
                TransitiveVerbMainClauseDescription(
                    TransitiveVerbDescription(
                        "have",
                        ownerDescription,
                        ownedDescription
                    )
                )
            LocativeOblique -> ObliquePredicatePossessionDescription(
                ownerDescription,
                ownedDescription,
                CaseValue.Locative
            )
            DativeOblique -> ObliquePredicatePossessionDescription(
                ownerDescription,
                ownedDescription,
                CaseValue.Dative
            )
            GenitiveOblique -> IntransitiveVerbMainClauseDescription(
                SimpleIntransitiveVerbDescription(
                    "exist",
                    ownedDescription.copyAndAddDefinitions(listOf(PossessorDescription(ownerDescription)))
                )
            )
        }.toClause(language, context, random)
}

class ObliquePredicatePossessionDescription(
    val ownerDescription: NominalDescription,
    val ownedDescription: NominalDescription,
    val case: CaseValue
) : SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        ObliquePredicatePossessionClause(
            language.lexis.getWordOrNull("exist")?.let { word ->
                IntransitiveVerbClause(
                    word.copyWithValues(
                        language.changeParadigm.syntaxLogic.resolveVerbForm(
                            language,
                            word.semanticsCore.speechPart,
                            context
                        )
                    ),
                    ownedDescription.toClause(language, context, random),
                    listOf(
                        CaseAdjunctClause(
                            ownerDescription.toClause(language, context, random),
                            case,
                            SyntaxRelation.PossessorAdjunct
                        )
                    )
                )
            }
                ?: throw SyntaxException("No verb 'exist' in Language"),
            when (context.type.first) {
                ContextValue.TypeContext.Simple -> VerbSentenceType.MainVerbClause
                ContextValue.TypeContext.GeneralQuestion -> VerbSentenceType.QuestionVerbClause
            }
        )
}