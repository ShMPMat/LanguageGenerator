package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.arranger.UndefinedArranger
import kotlin.random.Random


class VerbClause(
    val verb: Word,
    val additionalCategories: SourcedCategoryValues,
    val arguments: Map<SyntaxRelation, NominalClause>,
    val adjuncts: List<AdjunctClause> = listOf()
) : SyntaxClause {
    init {
        if (verb.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$verb is not a verb")
    }

    override fun toNode(language: Language, random: Random): SyntaxNode {
        val node = verb.toNode(
            SyntaxRelation.Verb,
            additionalCategories.map { it.categoryValue },
            UndefinedArranger
        )

        for ((relation, clause) in arguments) {
            val argumentNode = clause.toNode(language, random)
                .addThirdPerson()

            argumentNode.addRelevantCases(language.changeParadigm.syntaxLogic, node, relation)
            node.setRelationChild(relation, argumentNode)
        }

        for (adjunct in adjuncts)
            node.setRelationChild(adjunct.relation, adjunct.toNode(language, random))

        return node
    }
}

internal fun SyntaxNode.addThirdPerson(): SyntaxNode {
    if (categoryValues.none { it.parentClassName == personName })
        apply { categoryValues += PersonValue.Third }
    if (categoryValues.none { it.parentClassName == inclusivityName })
        apply { categoryValues += InclusivityValue.Exclusive }

    return this
}


private fun SyntaxNode.addRelevantCases(syntaxLogic: SyntaxLogic, verb: SyntaxNode, relation: SyntaxRelation) {
    val verbSpeechPart = verb.word.semanticsCore.speechPart

    categoryValues += syntaxLogic.resolveVerbCase(verbSpeechPart, relation)
}
