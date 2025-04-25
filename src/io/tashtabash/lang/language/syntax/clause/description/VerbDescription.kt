package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.clause.realization.IntransitiveVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.TransitiveVerbClause
import io.tashtabash.lang.language.syntax.context.ActorType
import io.tashtabash.lang.language.syntax.context.Context
import kotlin.random.Random


class TransitiveVerbDescription(
    val verb: Meaning,
    val actorDescription: NominalDescription,
    val patientDescription: NominalDescription,
    val indirectObjectDescriptions: List<IndirectObjectDescription> = listOf()
): ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(verb)?.let { word ->
            TransitiveVerbClause(
                word,
                language.changeParadigm.syntaxLogic.resolveVerbForm(language, word.semanticsCore.speechPart, context),
                actorDescription.toClause(language, context, random)
                    .copy(actorType = ActorType.Agent),
                patientDescription.toClause(language, context, random)
                    .copy(actorType = ActorType.Patient),
                indirectObjectDescriptions.map { obj ->
                    if (word.semanticsCore.tags.none { it.name == obj.type.name.lowercase() })
                        throw SyntaxException("Verb $verb does not support indirect object of type ${obj.type}")
                    obj.toClause(language, context, random)
                }
            )
        }
            ?: throw SyntaxException("No verb '$verb' in Language")
}


class IntransitiveVerbDescription(
    val verb: Meaning,
    val argumentDescription: NominalDescription,
    val indirectObjectDescriptions: List<IndirectObjectDescription> = listOf()
): ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random): IntransitiveVerbClause =
        language.lexis.getWordOrNull(verb)?.let { word ->
            IntransitiveVerbClause(
                word,
                language.changeParadigm.syntaxLogic.resolveVerbForm(language, word.semanticsCore.speechPart, context),
                argumentDescription.toClause(language, context, random)
                    .copy(actorType = ActorType.Agent),
                indirectObjectDescriptions.map { obj ->
                    if (word.semanticsCore.tags.none { it.name == obj.type.name.lowercase() })
                        throw SyntaxException("Verb $verb does not support indirect object of type ${obj.type}")
                    obj.toClause(language, context, random)
                }
            )
        }
            ?: throw SyntaxException("No verb '$verb' in Language")
}
