package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.syntax.numeral.NumeralParadigm


data class ChangeParadigm(
    val wordOrder: WordOrder,
    val wordChangeParadigm: WordChangeParadigm,
    val syntaxParadigm: SyntaxParadigm,
    val numeralParadigm: NumeralParadigm,
    val syntaxLogic: SyntaxLogic
) {
    fun mapApplicators(mapper: (CategoryApplicator) -> CategoryApplicator): ChangeParadigm =
        copy(
            wordChangeParadigm = wordChangeParadigm.mapApplicators(mapper)
        )

    override fun toString() = """
        |$numeralParadigm
        |
        |Word order:
        |$wordOrder
        |
        |
        |$syntaxParadigm
        |
        |$wordChangeParadigm
        |
        |$syntaxLogic
    """.trimMargin()
}
