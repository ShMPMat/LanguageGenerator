package shmp.language.category.paradigm

import shmp.language.syntax.Clause
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxRelation

internal class SentenceClauseConstructor(val paradigm: SentenceChangeParadigm) {
    private val processedNodes = mutableListOf<SentenceNode>()

    //TODO the first is always a Verb!
    fun applyNode(sentenceNode: SentenceNode): Clause {
        processedNodes.clear()
        return applyNodeInternal(sentenceNode, SyntaxRelation.Verb).second
    }

    private fun applyNodeInternal(sentenceNode: SentenceNode, relation: SyntaxRelation): NonJoinedClause {
        val categoryValues = computeValues(sentenceNode)

        val currentClause = relation to paradigm.wordChangeParadigm.apply(sentenceNode.word, categoryValues)
        processedNodes.add(sentenceNode)

        val childrenClauses = sentenceNode.relation
            .filter { it.value !in processedNodes }
            .map { (r, n) -> applyNodeInternal(n, r) }
            .toMutableList()

        return relation to paradigm.wordOrder.uniteToClause(currentClause, childrenClauses)
    }

    private fun computeValues(sentenceNode: SentenceNode): List<ParametrizedCategoryValue> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories
        return sentenceNode.extractValues(references)
    }
}

typealias NonJoinedClause = Pair<SyntaxRelation, Clause>
