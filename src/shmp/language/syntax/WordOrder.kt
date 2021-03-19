package shmp.language.syntax

import shmp.language.syntax.clause.translation.VerbSentenceType
import shmp.language.syntax.arranger.Arranger


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
