package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.context.DescriptionContext
import io.tashtabash.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class PredicatePossessionDescription(
    val owner: NominalDescription,
    val owned: NominalDescription
) : SentenceDescription() {
    override fun toClause(language: Language, context: DescriptionContext, random: Random) =
        language.changeParadigm.syntaxParadigm.predicatePossession.predicatePossession
            .randomUnwrappedElement()
            .apply(owner, owned)
            .toClause(language, context, random)
}

class ObliquePredicatePossessionDescription(
    val owner: NominalDescription,
    val owned: NominalDescription,
    val adjunctType: AdjunctType
) : SentenceDescription() {
    override fun toClause(language: Language, context: DescriptionContext, random: Random) =
        VerbMainClauseDescription(
            VerbDescription(
                "exist",
                mapOf(
                    MainObjectType.Argument to owned,
                    adjunctType to owner
                )
            )
        ).toClause(language, context, random)
}
