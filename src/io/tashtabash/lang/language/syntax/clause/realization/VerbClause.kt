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


class TransitiveVerbClause(
    val verb: Word,
    val additionalCategories: SourcedCategoryValues,
    val subjectClause: NominalClause,
    val objectClause: NominalClause,
    val adjuncts: List<AdjunctClause> = listOf()
) : SyntaxClause {
    init {
        if (verb.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$verb is not a verb")
        if (verb.semanticsCore.tags.any { it.name == "intrans" })
            throw SyntaxException("$verb in the transitive clause is intransitive")
    }

    override fun toNode(language: Language, random: Random): SyntaxNode {
        val node = verb.toNode(
            SyntaxRelation.Verb,
            additionalCategories.map { it.categoryValue },
            UndefinedArranger
        )
        val agent = subjectClause.toNode(language, random).addThirdPerson()
        val patient = objectClause.toNode(language, random).addThirdPerson()

        agent.addRelevantCases(language.changeParadigm.syntaxLogic, node, SyntaxRelation.Agent)
        patient.addRelevantCases(language.changeParadigm.syntaxLogic, node, SyntaxRelation.Patient)

        node.setRelationChild(SyntaxRelation.Agent, agent)
        node.setRelationChild(SyntaxRelation.Patient, patient)

        for (adjunct in adjuncts)
            node.setRelationChild(adjunct.relation, adjunct.toNode(language, random))

        return node
    }
}

class IntransitiveVerbClause(
    val verb: Word,
    val additionalCategories: SourcedCategoryValues,
    val argumentClause: NominalClause,
    val adjuncts: List<AdjunctClause> = listOf()
) : SyntaxClause {
    init {
        if (verb.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$verb is not a verb")
        if (verb.semanticsCore.tags.any { it.name == "trans" })
            throw SyntaxException("$verb in the intransitive clause is transitive")
    }

    override fun toNode(language: Language, random: Random): SyntaxNode {
        val node = verb.toNode(
            SyntaxRelation.Verb,
            additionalCategories.map { it.categoryValue },
            UndefinedArranger
        )
        val argument = argumentClause.toNode(language, random).addThirdPerson()

        argument.addRelevantCases(language.changeParadigm.syntaxLogic, node, SyntaxRelation.Argument)

        node.setRelationChild(SyntaxRelation.Argument, argument)

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
    val caseRelevantCategories = verb.categoryValues
        .filterIsInstance<TenseValue>()
        .toSet()
    val verbSpeechPart = verb.word.semanticsCore.speechPart

    categoryValues.removeIf { it is CaseValue }
    categoryValues += syntaxLogic.resolveVerbCase(verbSpeechPart, relation, caseRelevantCategories)
}
