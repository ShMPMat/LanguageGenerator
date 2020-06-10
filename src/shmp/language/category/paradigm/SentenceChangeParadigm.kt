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
    fun apply(sentence: Sentence): Clause = SentenceClauseConstructor(this).applyNode(sentence.node)

    override fun toString() = """
        |Word order: $wordOrder
        |
        |$wordChangeParadigm
    """.trimMargin()
}




typealias ReferenceHandler = (SpeechPart, Category) -> CategorySource?
