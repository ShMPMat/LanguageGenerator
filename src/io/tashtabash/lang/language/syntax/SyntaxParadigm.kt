package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.clause.construction.PotentialConstruction
import io.tashtabash.lang.language.syntax.features.CopulaPresence
import io.tashtabash.lang.language.syntax.features.PredicatePossessionPresence


data class SyntaxParadigm(
    val copula: CopulaPresence,
    val predicatePossession: PredicatePossessionPresence,
    val potential: PotentialConstruction
) {
    override fun toString() = """
        |Copula: $copula
        |
        |Predicate possession: $predicatePossession
        |
        |Potential: $potential
        |""".trimMargin()
}
