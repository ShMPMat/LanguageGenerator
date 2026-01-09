package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.clause.realization.NominalClause
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import kotlin.random.Random


open class NominalDescription(
    val noun: Meaning,
    val actorCompliment: ActorComplimentValue,
    val definitions: List<NounDefinerDescription> = listOf()
) : ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random): NominalClause =
        language.lexis.getWord(noun).let { word ->
            NominalClause(
                word,
                definitions.map { it.toClause(language, context, random) },
                language.changeParadigm.syntaxLogic.resolveComplimentCategories(actorCompliment, word.semanticsCore.speechPart),
            )
        }

    open fun copyAndAddDefinitions(newDefinitions: List<NounDefinerDescription>) = NominalDescription(
        noun,
        actorCompliment,
        definitions + newDefinitions
    )

    open fun copyAndAddDefinitions(vararg newDefinitions: NounDefinerDescription) =
        copyAndAddDefinitions(newDefinitions.toList())
}

class PronounDescription(
    type: String,
    private val actor: ActorValue,
    definitions: List<NounDefinerDescription> = listOf()
): NominalDescription(type, ActorComplimentValue(actor.number, actor.deixis), definitions) {
    override fun toClause(language: Language, context: Context, random: Random): NominalClause {
        val clause = super.toClause(language, context, random)

        // additionalCategories from clause are skipped because resolvePronounCategories resolves them anew
        return NominalClause(
            clause.nominal,
            clause.definitions,
            language.changeParadigm.syntaxLogic.resolvePronounCategories(actor, clause.nominal.semanticsCore.speechPart),
        )
    }

    override fun copyAndAddDefinitions(newDefinitions: List<NounDefinerDescription>) = PronounDescription(
        noun,
        actor,
        definitions + newDefinitions
    )
}
