package shmp.language.category.paradigm

import shmp.language.Language
import shmp.language.syntax.WordSequence
import shmp.language.syntax.Sentence
import shmp.language.syntax.WordOrder
import kotlin.random.Random


class SentenceChangeParadigm(
    val wordOrder: WordOrder,
    val wordChangeParadigm: WordChangeParadigm
) {
    fun apply(sentence: Sentence, language: Language, random: Random): WordSequence
            = SentenceClauseConstructor(this, sentence.type, random)
        .applyNode(sentence.syntaxClause.toNode(language))

    override fun toString() = """
        |Word order: $wordOrder
        |
        |$wordChangeParadigm
    """.trimMargin()
}
