package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.clause.syntax.VerbSentenceType
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.clause.syntax.CopulaSentenceType
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.utils.MapWithDefault


data class WordOrder(
    val sovOrder: MapWithDefault<VerbSentenceType, RandomOrder>,
    val copulaOrder: Map<CopulaType, MapWithDefault<CopulaSentenceType, Arranger>>,
    val nominalGroupOrder: NominalGroupOrder
) {
    override fun toString() = """
        |$sovOrder
        |
        |$copulaOrder
        |
        |$nominalGroupOrder
    """.trimMargin()
}
