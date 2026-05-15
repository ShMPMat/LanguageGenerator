package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SubstitutingOrder
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.arranger.UndefinedArranger
import kotlin.collections.plus
import kotlin.random.Random


interface PredicateClause: SyntaxClause {
    val head: Word
    val additionalCategories: SourcedCategoryValues

    fun withCategories(additionalCategories: SourcedCategoryValues): PredicateClause
    fun addAdjunct(adjunct: AdjunctClause): PredicateClause
}

data class VerbClause(
    val verb: Word,
    override val additionalCategories: SourcedCategoryValues,
    val arguments: Map<SyntaxRelation, NominalClause>,
    val adjuncts: List<AdjunctClause> = listOf()
) : PredicateClause {
    override val head = verb

    init {
        if (verb.semanticsCore.speechPart.type != SpeechPart.Verb)
            throw SyntaxException("$verb is not a verb")
    }

    override fun toNode(language: Language, random: Random): SyntaxNode {
        val node = verb.toNode(
            Predicate,
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

        val defaultArranger = language.changeParadigm.wordOrder.sovOrder
        node.arranger =
            if (arguments.containsKey(Argument))
                RelationArranger(SubstitutingOrder(defaultArranger, mapOf(Agent to Argument)))
            else
                RelationArranger(defaultArranger)

        return node
    }

    override fun withCategories(additionalCategories: SourcedCategoryValues): VerbClause =
        copy(additionalCategories = additionalCategories)

    override fun addAdjunct(adjunct: AdjunctClause): VerbClause =
        copy(adjuncts = adjuncts + adjunct)
}

data class AuxVerbClause(
    val aux: Word,
    val governedClause: PredicateClause, // Contains the relevant categories
    override val additionalCategories: SourcedCategoryValues,
    val arranger: Arranger
): PredicateClause {
    override val head = aux

    override fun toNode(
        language: Language,
        random: Random
    ): SyntaxNode {
        val node = aux.toNode(
            Auxiliary,
            additionalCategories.map { it.categoryValue },
            arranger
        )
        node.setRelationChild(Predicate, governedClause.toNode(language, random))
        return node
    }

    override fun withCategories(additionalCategories: SourcedCategoryValues): AuxVerbClause =
        copy(additionalCategories = additionalCategories)

    override fun addAdjunct(adjunct: AdjunctClause): AuxVerbClause =
        copy(governedClause = governedClause.addAdjunct(adjunct))
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
