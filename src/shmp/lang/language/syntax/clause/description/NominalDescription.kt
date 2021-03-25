package shmp.lang.language.syntax.clause.description

import shmp.lang.language.CategoryValue
import shmp.lang.language.Language
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.clause.realization.NominalClause
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.context.Context
import kotlin.random.Random


open class NominalDescription(
    val noun: Meaning,
    val definitions: List<NounDefinerDescription>,
    val additionalCategories: List<CategoryValue> = listOf()
) : ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(noun)?.let { word ->
            NominalClause(
                word,
                definitions.map { it.toClause(language, context, random) },
                additionalCategories,
                null
            )
        }
            ?: throw SyntaxException("No noun or pronoun '$noun' in Language")
}

class PersonalPronounDescription(
    definitions: List<NounDefinerDescription>,
    private val actorType: ActorType
): NominalDescription("_personal_pronoun", definitions) {
    override fun toClause(language: Language, context: Context, random: Random): NominalClause {
        val clause = super.toClause(language, context, random)

        return NominalClause(
            clause.nominal,
            clause.definitions,
            language.changeParadigm.syntaxLogic.resolvePronounCategories(context.actors.getValue(actorType)),
            actorType
        )
    }
}
