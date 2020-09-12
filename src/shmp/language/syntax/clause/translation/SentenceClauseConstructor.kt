package shmp.language.syntax.clause.translation

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.category.paradigm.SentenceChangeParadigm
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.WordSequence


class SentenceClauseConstructor(
    private val paradigm: SentenceChangeParadigm
) {
    internal fun applyNode(sentenceNode: SentenceNode, relation: SyntaxRelation): NonJoinedClause {
        val categoryValues = computeValues(sentenceNode)

        val currentClause = relation to paradigm.wordChangeParadigm.apply(sentenceNode.word, categoryValues)

        val childrenClauses = sentenceNode.children
            .map { (r, n) -> applyNode(n, r) }

        return relation to sentenceNode.orderer.orderClauses(listOf(currentClause) + childrenClauses)
    }

    private fun computeValues(sentenceNode: SentenceNode): List<ParametrizedCategoryValue> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories

        return sentenceNode.extractValues(references)
    }
}


typealias NonJoinedClause = Pair<SyntaxRelation, WordSequence>
