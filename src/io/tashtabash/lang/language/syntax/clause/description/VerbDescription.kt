package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.lexis.SemanticsTag
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.realization.CaseAdjunctClause
import io.tashtabash.lang.language.syntax.clause.realization.IntransitiveVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.TransitiveVerbClause
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

            val arguments = argDescriptions.mapKeys {
                language.changeParadigm
                    .syntaxLogic
                    .resolveArgumentTypes(verb.semanticsCore.speechPart, it.key)
            }

            if (verb.semanticsCore.tags.contains(SemanticsTag("intrans")))
                IntransitiveVerbClause(
                    verb,
                    categoryValues,
                    arguments.getValue(SyntaxRelation.Argument)
                        .toClause(language, context, random)
                        .copy(actorType = SyntaxRelation.Argument),
                    constructCaseAdjunctClauses(arguments, language, context, random)
                )
            else
                TransitiveVerbClause(
                    verb,
                    categoryValues,
                    arguments.getValue(SyntaxRelation.Agent)
                        .toClause(language, context, random)
                        .copy(actorType = SyntaxRelation.Agent),
                    arguments.getValue(SyntaxRelation.Patient)
                        .toClause(language, context, random)
                        .copy(actorType = SyntaxRelation.Patient),
                    constructCaseAdjunctClauses(arguments, language, context, random)
                )
        }
            ?: throw SyntaxException("No verb '$verb' in Language")

    private fun constructCaseAdjunctClauses(
        arguments: Map<SyntaxRelation, NominalDescription>,
        language: Language,
        context: Context,
        random: Random
    ) = arguments.filterNot { (objectType) -> objectType in MainObjectType.syntaxRelations }
        .map { (syntaxRelation, description) ->
            CaseAdjunctClause(description.toClause(language, context, random), syntaxRelation)
        }
}
