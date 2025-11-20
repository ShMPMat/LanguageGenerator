package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.toIntransitive
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.realization.CaseAdjunctClause
import io.tashtabash.lang.language.syntax.clause.realization.IntransitiveVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.TransitiveVerbClause
import io.tashtabash.lang.language.syntax.context.ActorType
import io.tashtabash.lang.language.syntax.context.Context
import kotlin.random.Random


class VerbDescription(
    val verb: Meaning,
    val argDescriptions: Map<ObjectType, NominalDescription>
): ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWordOrNull(verb)?.let { verb ->
            val categoryValues = language.changeParadigm.syntaxLogic.resolveVerbForm(
                language,
                verb.semanticsCore.speechPart,
                context
            )

            if (verb.semanticsCore.speechPart == SpeechPart.Verb.toIntransitive())
                IntransitiveVerbClause(
                    verb,
                    categoryValues,
                    argDescriptions.getValue(MainObjectType.Argument)
                        .toClause(language, context, random)
                        .copy(actorType = ActorType.Agent),
                    constructCaseAdjunctClauses(language, context, random, false)
                )
            else {
                TransitiveVerbClause(
                        verb,
                    categoryValues,
                    argDescriptions.getValue(MainObjectType.Agent)
                        .toClause(language, context, random)
                        .copy(actorType = ActorType.Agent),
                    argDescriptions.getValue(MainObjectType.Patient)
                        .toClause(language, context, random)
                        .copy(actorType = ActorType.Agent),
                    constructCaseAdjunctClauses(language, context, random, true)
                )
            }
        }
            ?: throw SyntaxException("No verb '$verb' in Language")

    private fun constructCaseAdjunctClauses(
        language: Language,
        context: Context,
        random: Random,
        isTransitive: Boolean
    ) = argDescriptions
        .filterNot { (objectType) ->
            objectType.relation in listOf(SyntaxRelation.Argument, SyntaxRelation.Agent)
                || isTransitive && objectType.relation == SyntaxRelation.Patient
        }.map { (objectType, description) ->
            CaseAdjunctClause(description.toClause(language, context, random), objectType.relation)
        }
}
