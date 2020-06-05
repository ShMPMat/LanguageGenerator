package shmp.language.category.paradigm

import shmp.language.SovOrder
import shmp.language.SpeechPart
import shmp.language.category.Category
import shmp.language.category.CategorySource
import shmp.language.syntax.Clause
import shmp.language.syntax.Sentence
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxRelation

class SentenceChangeParadigm(
    val sovOrder: SovOrder,
    val wordChangeParadigm: WordChangeParadigm,
    val referenceHandler: ReferenceHandler
) {
    private val processedNodes = mutableListOf<SentenceNode>()//TODO to a new class

    fun apply(sentence: Sentence): Clause {
        val clausesInParadigm = applyNode(sentence.node)
        val resultWords = clausesInParadigm
            .sortedBy { (r) ->
                val i = sovOrder.referenceOrder.indexOf(r)
                if (i == -1) throw ChangeException("No Relation $r in a relation order ${sovOrder.referenceOrder}")
                i
            }.flatMap { it.second.words }
        processedNodes.clear()
        return Clause(resultWords)
    }

    //TODO the first is always a Verb!
    fun applyNode(sentenceNode: SentenceNode): List<NonJoinedClause> =
        applyNodeInternal(sentenceNode, SyntaxRelation.Verb)

    private fun applyNodeInternal(sentenceNode: SentenceNode, relation: SyntaxRelation): List<NonJoinedClause> {
        val speechPart = sentenceNode.word.semanticsCore.speechPart
        val references = wordChangeParadigm.getSpeechPartParadigm(speechPart).categories
            .map {
                val reference = referenceHandler(speechPart, it)
                    ?: throw ChangeException("$speechPart doesn't have a category ${it.outType}")
                it to reference
            }
        val categoryValues = sentenceNode.extractValues(references)
        val currentClause = relation to wordChangeParadigm.apply(sentenceNode.word, categoryValues)
        processedNodes.add(sentenceNode)
        val childrenClauses = sentenceNode.relation
            .filter { it.value !in processedNodes }
            .flatMap { (r, n) -> applyNodeInternal(n, r) }
            .toMutableList()
        childrenClauses.add(currentClause)
        return childrenClauses
    }

    override fun toString() = """
        |SOV order: $sovOrder
        |
        |$wordChangeParadigm
    """.trimMargin()
}


typealias ReferenceHandler = (SpeechPart, Category) -> CategorySource?

typealias NonJoinedClause = Pair<SyntaxRelation, Clause>
