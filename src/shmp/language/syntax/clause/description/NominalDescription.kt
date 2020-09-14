package shmp.language.syntax.clause.description

import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.lexis.Meaning
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.clause.realization.NominalClause
import kotlin.random.Random


class NominalDescription(
    val noun: Meaning,
    val definitions: List<NounDefinerDescription>,
    val additionalCategories: List<CategoryValue> = listOf()
) : ClauseDescription {
    override fun toClause(language: Language, random: Random) =
        language.lexis.getWordOrNull(noun)?.let { word ->
            NominalClause(
                word,
                definitions.map { it.toClause(language, random) },
                additionalCategories
            )
        }
            ?: throw SyntaxException("No noun '$noun' in Language")
}
