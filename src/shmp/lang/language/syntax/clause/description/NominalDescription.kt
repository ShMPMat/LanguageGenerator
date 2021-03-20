package shmp.lang.language.syntax.clause.description

import shmp.lang.language.CategoryValue
import shmp.lang.language.Language
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.clause.realization.NominalClause
import shmp.lang.language.syntax.clause.realization.SyntaxClause
import shmp.lang.language.syntax.context.Context
import kotlin.random.Random


class NominalDescription(
    val noun: Meaning,
    val definitions: List<NounDefinerDescription>,
    val additionalCategories: List<CategoryValue> = listOf()
) : ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(noun)?.let { word ->
            NominalClause(
                word,
                definitions.map { it.toClause(language, context, random) },
                additionalCategories
            )
        }
            ?: throw SyntaxException("No noun '$noun' in Language")
}
