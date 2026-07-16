package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.NegationValue
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.*
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import kotlin.random.Random


abstract class SentenceClause : UnfoldableClause {
    final override fun unfold(language: Language, random: Random): WordSequence {
        val node = toNode(language, random)
        applyTransformers(node, language.changeParadigm.syntaxLogic)

        return SyntaxNodeTranslator(language.changeParadigm)
            .applyNode(node, random)
    }

    // The transformers are applied to sentence heads and then recursively to it's children
    private fun applyTransformers(node: SyntaxNode, syntaxLogic: SyntaxLogic) =
        syntaxLogic.applyTransformers(node)
}


fun SyntaxNode.addQuestionTag() {
    tags += SyntaxNodeTag.Question // Used to allow word order shenanigans
}

// This ad-hoc propagation is rickety, but I don't know how else to push topic info from the sentence level
// to the right argument
private fun SyntaxNode.addTopic(relation: SyntaxRelation) {
    relations[relation]
        ?.let { it.tags += SyntaxNodeTag.Topic }

    // Propagate through Aux constructions
    relations[SyntaxRelation.Predicate]
        ?.addTopic(relation)
}

data class VerbSentenceClause(
    val predicate: PredicateClause,
    val type: List<VerbSentenceType>,
    val topic: SyntaxRelation? // Any of the verb's children
) : SentenceClause() {
    override fun toNode(language: Language, random: Random): SyntaxNode =
        predicate.toNode(language, random).apply {
            if (VerbSentenceType.QuestionVerbClause in type)
                addQuestionTag()
            if (VerbSentenceType.NegatedVerbClause in type)
                categoryValues += NegationValue.Negative
            topic?.let { addTopic(it) }
        }
}

class CopulaSentenceClause(
    private val copulaClause: CopulaClause,
    val type: List<CopulaSentenceType>
) : SentenceClause() {
    override fun toNode(language: Language, random: Random): SyntaxNode =
        copulaClause.toNode(language, random).apply {
            arranger = language.changeParadigm
                .wordOrder
                .copulaOrder
                .getValue(copulaClause.copulaType)
                .getValue(type.firstOrNull() ?: CopulaSentenceType.MainCopulaClause)
            if (CopulaSentenceType.QuestionCopulaClause in type)
                addQuestionTag()
            if (CopulaSentenceType.NegatedCopulaClause in type)
                categoryValues += NegationValue.Negative
        }
}
