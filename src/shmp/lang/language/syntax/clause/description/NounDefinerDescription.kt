package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.clause.realization.AdjectiveClause
import shmp.lang.language.syntax.clause.realization.NounDefinerClause
import shmp.lang.language.syntax.clause.realization.PossessorClause
import shmp.lang.language.syntax.clause.realization.SyntaxClause
import shmp.lang.language.syntax.context.Context
import kotlin.random.Random


abstract class NounDefinerDescription : ClauseDescription {
    abstract override fun toClause(language: Language, context: Context, random: Random): NounDefinerClause
}


class AdjectiveDescription(val adjective: Meaning) : NounDefinerDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(adjective)?.let { word ->
            AdjectiveClause(word)
        }
            ?: throw SyntaxException("No adjective '$adjective' in Language")
}

class PossessorDescription(val nominalDescription: NominalDescription) : NounDefinerDescription() {
    override fun toClause(language: Language, context: Context, random: Random) =
        PossessorClause(nominalDescription.toClause(language, context, random))
}
