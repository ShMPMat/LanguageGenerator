package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.lexis.Meaning
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.clause.realization.TransitiveVerbClause
import kotlin.random.Random


class TransitiveVerbDescription(
    val verb: Meaning,
    val subjectDescription: NominalDescription,
    val objectDescription: NominalDescription
): ClauseDescription {
    override fun toClause(language: Language, random: Random) =
        language.lexis.getWordOrNull(verb)?.let { word ->
            TransitiveVerbClause(
                word,
                subjectDescription.toClause(language, random),
                objectDescription.toClause(language, random)
            )
        }
            ?: throw SyntaxException("No verb '$verb' in Language")
}
