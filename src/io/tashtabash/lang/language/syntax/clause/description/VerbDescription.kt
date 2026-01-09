package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.realization.CaseAdjunctClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbClause
import io.tashtabash.lang.language.syntax.context.Context
import kotlin.random.Random


class VerbDescription(val verb: Meaning, val args: Map<ObjectType, NominalDescription>): ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random) =
        language.lexis.getWord(verb).let { verb ->
            val categoryValues = language.changeParadigm.syntaxLogic.resolveVerbForm(
                language,
                verb.semanticsCore.speechPart,
                context
            )

            val resolvedArgs = args.mapKeys {
                language.changeParadigm
                    .syntaxLogic
                    .resolveArgumentTypes(verb.semanticsCore.speechPart, it.key)
            }
            val directArguments = resolvedArgs.filter { (objectType) -> objectType in MainObjectType.syntaxRelations }

            VerbClause(
                verb,
                categoryValues,
                directArguments.mapValues { (_, v) -> v.toClause(language, context, random) },
                constructCaseAdjunctClauses(resolvedArgs, language, context, random)
            )
        }

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
