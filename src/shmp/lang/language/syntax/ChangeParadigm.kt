package shmp.lang.language.syntax

import shmp.lang.language.category.paradigm.WordChangeParadigm


class ChangeParadigm(
    val wordOrder: WordOrder,
    val wordChangeParadigm: WordChangeParadigm,
    val syntaxParadigm: SyntaxParadigm,
    val syntaxLogic: SyntaxLogic
) {
    override fun toString() = """
        |Word order: 
        |$wordOrder
        |
        |$syntaxParadigm
        |
        |$wordChangeParadigm
        |
        |$syntaxLogic
    """.trimMargin()
}
