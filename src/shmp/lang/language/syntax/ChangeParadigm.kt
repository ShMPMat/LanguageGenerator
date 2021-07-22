package shmp.lang.language.syntax

import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.syntax.numeral.NumeralParadigm


class ChangeParadigm(
    val wordOrder: WordOrder,
    val wordChangeParadigm: WordChangeParadigm,
    val syntaxParadigm: SyntaxParadigm,
    val numeralParadigm: NumeralParadigm,
    val syntaxLogic: SyntaxLogic
) {
    override fun toString() = """
        |$numeralParadigm
        |
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
