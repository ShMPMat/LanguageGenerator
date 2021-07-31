package shmp.lang.language.syntax.clause.translation

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.*
import kotlin.random.Random


class SentenceClauseTranslator(private val paradigm: ChangeParadigm) {
    internal fun applyNode(node: SentenceNode, random: Random): WordSequence {
        node.nodesOrder = node.arranger.order(node.allRelations.map { it to it }, random)

        val categoryValues = computeValues(node)
        val currentClause = node.typeForChildren to paradigm.wordChangeParadigm.apply(
            node.word.copy(categoryValues = listOf()),
            categoryValues = categoryValues
        )
        val childrenClauses = node.children
            .map { it to applyNode(it.second, random) }
            .filter { !it.first.second.isDropped }
            .map { it.first.first to it.second.setInPlace() }

        return (childrenClauses + currentClause)
            .sortedBy { node.nodesOrder.indexOf(it.first) }
            .map { it.second }
            .reduceRight(FoldedWordSequence::plus)
            .unfold()
    }

    private fun computeValues(sentenceNode: SentenceNode): List<SourcedCategoryValue> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories

        return sentenceNode.extractValues(references)
    }
}


typealias NonJoinedClause = Pair<SyntaxRelation, WordSequence>
