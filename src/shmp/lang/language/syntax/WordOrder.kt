package shmp.lang.language.syntax

import shmp.lang.language.syntax.clause.translation.VerbSentenceType
import shmp.lang.language.syntax.arranger.Arranger


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
