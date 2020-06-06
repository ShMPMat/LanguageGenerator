package shmp.language.category.paradigm

import shmp.language.syntax.Clause
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxRelation

internal class SentenceClauseConstructor(val paradigm: SentenceChangeParadigm) {
    private val processedNodes = mutableListOf<SentenceNode>()

    //TODO the first is always a Verb!
    fun applyNode(sentenceNode: SentenceNode): List<NonJoinedClause> =
        applyNodeInternal(sentenceNode, SyntaxRelation.Verb)

    private fun applyNodeInternal(sentenceNode: SentenceNode, relation: SyntaxRelation): List<NonJoinedClause> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories
            .map {
                val reference = paradigm.referenceHandler(speechPart, it.category)
                    ?: throw ChangeException("$speechPart doesn't have a category ${it.category.outType}")
                it.category to reference
            }
        val categoryValues = sentenceNode.extractValues(references).map { ParametrizedCategoryValue(it.first) }
        val currentClause = relation to paradigm.wordChangeParadigm.apply(sentenceNode.word, categoryValues)
        processedNodes.add(sentenceNode)
        val childrenClauses = sentenceNode.relation
            .filter { it.value !in processedNodes }
            .flatMap { (r, n) -> applyNodeInternal(n, r) }
            .toMutableList()
        childrenClauses.add(currentClause)
        return childrenClauses
    }
}

typealias NonJoinedClause = Pair<SyntaxRelation, Clause>
