package shmp.lang.language.syntax.clause.translation

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence
import kotlin.random.Random


class SentenceClauseTranslator(private val paradigm: ChangeParadigm) {
    internal fun applyNode(sentenceNode: SentenceNode, random: Random): WordSequence {
        val categoryValues = computeValues(sentenceNode)

        val currentClause =
            sentenceNode.typeForChildren to paradigm.wordChangeParadigm.apply(
                sentenceNode.word.copy(categoryValues = listOf()),
                categoryValues
            )

        val childrenClauses = sentenceNode.children
            .map { it to applyNode(it.second, random) }
            .filter { !it.first.second.isDropped }
            .map { it.first.first to it.second }

        if (sentenceNode.typeForChildren == SyntaxRelation.Nominal) {
            val k = 0
        }

        return sentenceNode.arranger.orderClauses(listOf(currentClause) + childrenClauses, random)
    }

    private fun computeValues(sentenceNode: SentenceNode): List<SourcedCategoryValue> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart).categories

        return sentenceNode.extractValues(references)
    }
}


typealias NonJoinedClause = Pair<SyntaxRelation, WordSequence>
