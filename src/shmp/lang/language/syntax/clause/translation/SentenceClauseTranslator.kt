package shmp.lang.language.syntax.clause.translation

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence
import kotlin.random.Random


class SentenceClauseTranslator(private val paradigm: ChangeParadigm) {
    internal fun applyNode(sentenceNode: SentenceNode, relation: SyntaxRelation, random: Random): NonJoinedClause {
        val categoryValues = computeValues(sentenceNode)

        val currentClause =
            sentenceNode.typeForChildren to paradigm.wordChangeParadigm.apply(
                sentenceNode.word.copy(categoryValues = listOf()),
                categoryValues
            )

        val childrenClauses = sentenceNode.children
            .map { it to applyNode(it.second, it.first, random) }
            .filter { !it.first.second.isDropped }
            .map { it.second }

        return relation to sentenceNode.arranger.orderClauses(listOf(currentClause) + childrenClauses, random)
    }

    private fun computeValues(sentenceNode: SentenceNode): List<SourcedCategoryValue> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories

        return sentenceNode.extractValues(references)
    }
}


typealias NonJoinedClause = Pair<SyntaxRelation, WordSequence>
