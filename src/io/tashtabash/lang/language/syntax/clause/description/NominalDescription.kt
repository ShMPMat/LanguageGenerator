package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.clause.realization.NominalClause
import io.tashtabash.lang.language.syntax.context.ActorType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import kotlin.random.Random


open class NominalDescription(
    val noun: Meaning,
    val definitions: List<NounDefinerDescription>,
    val actorCompliment: ActorComplimentValue
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

    open fun copyAndAddDefinitions(newDefinitions: List<NounDefinerDescription>) = NominalDescription(
        noun,
        definitions + newDefinitions,
        actorCompliment
    )
}

class PronounDescription(
    type: String,
    definitions: List<NounDefinerDescription>,
    private val actorType: ActorType,
    private val actor: ActorValue
): NominalDescription(type, definitions, ActorComplimentValue(actor.number, actor.deixis)) {
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

    override fun copyAndAddDefinitions(newDefinitions: List<NounDefinerDescription>) = PronounDescription(
        noun,
        definitions + newDefinitions,
        actorType,
        actor
    )
}
