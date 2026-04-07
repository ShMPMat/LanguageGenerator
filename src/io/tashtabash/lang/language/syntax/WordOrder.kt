package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.clause.syntax.VerbSentenceType
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import io.tashtabash.lang.language.syntax.clause.syntax.CopulaSentenceType
import io.tashtabash.lang.utils.MapWithDefault


data class WordOrder(
    val sovOrder: MapWithDefault<VerbSentenceType, RandomOrder>,
    val copulaOrder: Map<CopulaConstruction, MapWithDefault<CopulaSentenceType, Arranger>>,
    val nominalGroupOrder: NominalGroupOrder
) {
    override fun toString() = """
        |Verb:
        |$sovOrder
        |
        |Copula:
        |${copulaOrder.entries.joinToString("\n")}
        |
        |Noun Group: $nominalGroupOrder
    """.trimMargin()
}
