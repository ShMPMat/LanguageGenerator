package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.lexis.Meaning
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.clause.realization.AdjectiveClause
import shmp.language.syntax.clause.realization.NounDefinerClause
import kotlin.random.Random


abstract class NounDefinerDescription : ClauseDescription {
    abstract override fun toClause(language: Language, random: Random): NounDefinerClause
}


class AdjectiveDescription(val adjective: Meaning) : NounDefinerDescription() {
    override fun toClause(language: Language, random: Random) =
        language.lexis.getWordOrNull(adjective)?.let { word ->
            AdjectiveClause(word)
        }
            ?: throw SyntaxException("No adjective '$adjective' in Language")
}
