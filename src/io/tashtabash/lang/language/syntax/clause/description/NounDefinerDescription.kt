package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.clause.realization.AdjectiveClause
import io.tashtabash.lang.language.syntax.clause.realization.NounDefinerClause
import io.tashtabash.lang.language.syntax.clause.realization.PossessorClause
import io.tashtabash.lang.language.syntax.clause.realization.SyntaxClause
import io.tashtabash.lang.language.syntax.context.Context
import kotlin.random.Random


abstract class NounDefinerDescription : ClauseDescription {
    abstract override fun toClause(language: Language, context: Context, random: Random): NounDefinerClause
}


class AdjectiveDescription(val adjective: Meaning) : NounDefinerDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(adjective)?.let { word ->
            AdjectiveClause(
                word,
                language.changeParadigm.syntaxLogic.resolveAdjectiveForm(language, word.semanticsCore.speechPart, context)
            )
        }
            ?: throw SyntaxException("No adjective '$adjective' in Language")
}

class PossessorDescription(val nominalDescription: NominalDescription) : NounDefinerDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        PossessorClause(nominalDescription.toClause(language, context, random))
}
