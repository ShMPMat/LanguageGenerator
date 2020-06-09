package shmp.language.category.paradigm

import shmp.language.SpeechPart
import shmp.language.category.Category
import shmp.language.category.CategorySource
import shmp.language.syntax.Clause
import shmp.language.syntax.Sentence
import shmp.language.syntax.WordOrder

class SentenceChangeParadigm(
    val wordOrder: WordOrder,
    val wordChangeParadigm: WordChangeParadigm
) {
    fun apply(sentence: Sentence): Clause {
        val clausesInParadigm = SentenceClauseConstructor(this).applyNode(sentence.node)
        return joinClauses(clausesInParadigm)
    }

    private fun joinClauses(clauses: List<NonJoinedClause>): Clause {
        val resultWords = clauses
            .sortedBy { (r) ->
                val i = wordOrder.sovOrder.referenceOrder.indexOf(r)
                if (i == -1)
                    throw ChangeException("No Relation $r in a relation order ${wordOrder.sovOrder.referenceOrder}")
                i
            }.flatMap { it.second.words }
        return Clause(resultWords)
    }

    override fun toString() = """
        |Word order: $wordOrder
        |
        |$wordChangeParadigm
    """.trimMargin()
}




typealias ReferenceHandler = (SpeechPart, Category) -> CategorySource?
