package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.arranger.Arranger


data class WordOrder(
    val sovOrder: Map<VerbSentenceType, SovOrder>,
    val copulaOrder: Map<CopulaWordOrder, Arranger>,
    val nominalGroupOrder: NominalGroupOrder
) {
    override fun toString() = """
        |$sovOrder
        |
        |$copulaOrder
        |
        |$nominalGroupOrder
        |
    """.trimMargin()
}
