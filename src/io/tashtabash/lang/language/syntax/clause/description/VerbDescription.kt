package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.clause.realization.IntransitiveVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.TransitiveVerbClause
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
                word.copyWithValues(
                    language.changeParadigm.syntaxLogic.resolveVerbForm(language, word.semanticsCore.speechPart, context)
                ),
                actorDescription.toClause(language, context, random),
                patientDescription.toClause(language, context, random),
                indirectObjectDescriptions.map { obj ->
                    if (word.semanticsCore.tags.none { it.name == obj.type.name.toLowerCase() })
                        throw SyntaxException("Verb $verb does not support indirect object of type ${obj.type}")
                    obj.toClause(language, context, random)
                }
            )
        }
            ?: throw SyntaxException("No verb '$verb' in Language")
}


interface IntransitiveVerbDescription: ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random): IntransitiveVerbClause
}

class SimpleIntransitiveVerbDescription(
    val verb: Meaning,
    val argumentDescription: NominalDescription,
    val indirectObjectDescriptions: List<IndirectObjectDescription> = listOf()
): IntransitiveVerbDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(verb)?.let { word ->
            IntransitiveVerbClause(
                word.copyWithValues(
                    language.changeParadigm.syntaxLogic.resolveVerbForm(language, word.semanticsCore.speechPart, context)
                ),
                argumentDescription.toClause(language, context, random),
                indirectObjectDescriptions.map { obj ->
                    if (word.semanticsCore.tags.none { it.name == obj.type.name.toLowerCase() })
                        throw SyntaxException("Verb $verb does not support indirect object of type ${obj.type}")
                    obj.toClause(language, context, random)
                }
            )
        }
            ?: throw SyntaxException("No verb '$verb' in Language")
}
