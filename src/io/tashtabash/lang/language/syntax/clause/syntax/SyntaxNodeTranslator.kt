package io.tashtabash.lang.language.syntax.clause.syntax

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.sequence.setInPlace
import io.tashtabash.lang.language.syntax.sequence.unfold
import kotlin.random.Random


class SyntaxNodeTranslator(private val paradigm: ChangeParadigm) {
    internal fun applyNode(node: SyntaxNode, random: Random): WordSequence {
        val nodesOrder = node.arranger.order(node.allTreeRelations.map { it to it }, random)

        val categoryValues = computeValues(node)
        val currentClause = node.typeForChildren to paradigm.wordChangeParadigm.apply(
            node.word.copy(categoryValues = listOf()),
            categoryValues
        ).words
        val childrenClauses = node.children
            .map { it to applyNode(it.second, random) }
            .filter { !it.first.second.isDropped }
            .map { it.first.first to it.second.setInPlace() }

        return (childrenClauses + currentClause)
            .sortedBy { nodesOrder.indexOf(it.first) }
            .map { it.second }
            .reduceRight(FoldedWordSequence::plus)
            .unfold()
    }

    private fun computeValues(syntaxNode: SyntaxNode): List<SourcedCategoryValue> {
        val speechPart = syntaxNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories

        return syntaxNode.extractValues(references)
    }
}


typealias NonJoinedClause = Pair<SyntaxRelation, WordSequence>
