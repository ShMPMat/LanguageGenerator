package shmp.lang.language.syntax.clause.description

import shmp.lang.language.CategoryValue
import shmp.lang.language.Language
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.clause.realization.NominalClause
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
