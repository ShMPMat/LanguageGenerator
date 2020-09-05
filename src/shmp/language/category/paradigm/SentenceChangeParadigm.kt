package shmp.language.category.paradigm

import shmp.language.syntax.Clause
import shmp.language.syntax.Sentence
import shmp.language.syntax.WordOrder
import kotlin.random.Random


class SentenceChangeParadigm(
    val wordOrder: WordOrder,
    val wordChangeParadigm: WordChangeParadigm
) {
    fun apply(sentence: Sentence, random: Random): Clause
            = SentenceClauseConstructor(this, sentence.type, random).applyNode(sentence.node)

    override fun toString() = """
        |Word order: $wordOrder
        |
        |$wordChangeParadigm
    """.trimMargin()
}
