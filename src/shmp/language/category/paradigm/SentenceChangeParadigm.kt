package shmp.language.category.paradigm

import shmp.language.SovOrder

class SentenceChangeParadigm(val sovOrder: SovOrder, val wordChangeParadigm: WordChangeParadigm) {
    override fun toString() = """
        |SOV order: $sovOrder
        |
        |$wordChangeParadigm
    """.trimMargin()
}