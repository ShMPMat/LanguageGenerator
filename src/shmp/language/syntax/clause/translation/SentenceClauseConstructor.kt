package shmp.language.syntax.clause.translation

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.WordSequence
import kotlin.random.Random


class SentenceClauseConstructor(
    private val paradigm: ChangeParadigm
) {
    internal fun applyNode(sentenceNode: SentenceNode, relation: SyntaxRelation, random: Random): NonJoinedClause {
        val categoryValues = computeValues(sentenceNode)

        val currentClause =
            sentenceNode.typeForChildren to paradigm.wordChangeParadigm.apply(sentenceNode.word, categoryValues)

        val childrenClauses = sentenceNode.children
            .map { (r, n) -> applyNode(n, r, random) }

        return relation to sentenceNode.arranger.orderClauses(listOf(currentClause) + childrenClauses, random)
    }

    private fun computeValues(sentenceNode: SentenceNode): List<ParametrizedCategoryValue> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories

        return sentenceNode.extractValues(references)
    }
}


typealias NonJoinedClause = Pair<SyntaxRelation, WordSequence>
