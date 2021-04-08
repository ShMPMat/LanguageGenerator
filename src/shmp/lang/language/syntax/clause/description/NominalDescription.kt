package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.clause.realization.NominalClause
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.context.ContextValue
import kotlin.random.Random


open class NominalDescription(
    val noun: Meaning,
    val definitions: List<NounDefinerDescription>,
    val actorCompliment: ContextValue.ActorComplimentValue
) : ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(noun)?.let { word ->
            NominalClause(
                word,
                definitions.map { it.toClause(language, context, random) },
                language.changeParadigm.syntaxLogic.resolveComplimentCategories(actorCompliment, word.semanticsCore.speechPart),
                null
            )
        }
            ?: throw SyntaxException("No noun or pronoun '$noun' in Language")
}

class PronounDescription(
    type: String,
    definitions: List<NounDefinerDescription>,
    private val actorType: ActorType,
    private val actor: ContextValue.ActorValue
): NominalDescription(type, definitions, ContextValue.ActorComplimentValue(actor.number, actor.deixis)) {
    override fun toClause(language: Language, context: Context, random: Random): NominalClause {
        val clause = super.toClause(language, context, random)

        context.actors[actorType] = actor

        return NominalClause(
            clause.nominal,
            clause.definitions,
            language.changeParadigm.syntaxLogic.resolvePronounCategories(actor, clause.nominal.semanticsCore.speechPart),
            actorType
        )
    }
}
