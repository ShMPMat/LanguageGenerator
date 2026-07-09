package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.clause.realization.AdjectiveClause
import io.tashtabash.lang.language.syntax.clause.realization.NounDefinerClause
import io.tashtabash.lang.language.syntax.clause.realization.PossessorClause
import io.tashtabash.lang.language.syntax.context.DescriptionContext
import kotlin.random.Random


abstract class NounDefinerDescription : ClauseDescription {
    abstract override fun toClause(language: Language, context: DescriptionContext, random: Random): NounDefinerClause
}


class AdjectiveDescription(val adjective: Meaning) : NounDefinerDescription() {
    override fun toClause(language: Language, context: DescriptionContext, random: Random) =
        language.lexis.getWord(adjective).let { word ->
            AdjectiveClause(
                word,
                language.changeParadigm.syntaxLogic.resolveAdjectiveForm(language, word.semanticsCore.speechPart, context)
            )
        }
}

class PossessorDescription(val nominalDescription: NominalDescription) : NounDefinerDescription() {
    override fun toClause(language: Language, context: DescriptionContext, random: Random) =
        PossessorClause(nominalDescription.toClause(language, context, random))
}
