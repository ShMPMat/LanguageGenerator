package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.features.PredicatePossessionType
import shmp.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class PredicatePossessionDescription(
    val ownerDescription: NominalDescription,
    val ownedDescription: NominalDescription
): SentenceDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        when (language.changeParadigm.syntaxParadigm.predicatePossessionPresence.predicatePossessionType.randomUnwrappedElement()) {
            PredicatePossessionType.HaveVerb ->
                TransitiveVerbMainClauseDescription(TransitiveVerbDescription("have", ownerDescription, ownedDescription))
        }.toClause(language, context, random)
}
