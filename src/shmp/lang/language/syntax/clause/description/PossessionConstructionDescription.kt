package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.features.PossessionConstructionType
import shmp.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class PossessionConstructionDescription(
    val ownerDescription: NominalDescription,
    val ownedDescription: NominalDescription
): SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        when (language.changeParadigm.syntaxParadigm.possessionConstructionPresence.possessionConstructionType.randomUnwrappedElement()) {
            PossessionConstructionType.HaveVerb ->
                TransitiveVerbMainClauseDescription(TransitiveVerbDescription("have", ownerDescription, ownedDescription))
        }.toClause(language, context, random)
}
