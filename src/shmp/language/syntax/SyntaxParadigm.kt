package shmp.language.syntax

import shmp.language.category.paradigm.WordChangeParadigm


class SyntaxParadigm(
    val wordOrder: WordOrder,
    val wordChangeParadigm: WordChangeParadigm
) {
    override fun toString() = """
        |Word order: $wordOrder
        |
        |$wordChangeParadigm
    """.trimMargin()
}
