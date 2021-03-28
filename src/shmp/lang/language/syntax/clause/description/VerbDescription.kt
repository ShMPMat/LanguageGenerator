package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.clause.realization.TransitiveVerbClause
import shmp.lang.language.syntax.context.Context
import kotlin.random.Random


class TransitiveVerbDescription(
    val verb: Meaning,
    val actorDescription: NominalDescription,
    val patientDescription: NominalDescription
): ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(verb)?.let { word ->
            TransitiveVerbClause(
                word.copyWithValues(language.changeParadigm.syntaxLogic.resolveVerbForm(language, context)),
                actorDescription.toClause(language, context, random),
                patientDescription.toClause(language, context, random)
            )
        }
            ?: throw SyntaxException("No verb '$verb' in Language")
}
