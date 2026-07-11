package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.features.CopulaPresence
import io.tashtabash.lang.language.syntax.features.PredicatePossessionPresence


data class SyntaxParadigm(
    val copula: CopulaPresence,
    val predicatePossession: PredicatePossessionPresence,
) {
    override fun toString() = """
        |Copula: $copula
        |
        |Predicate possession: $predicatePossession
        |""".trimMargin()
}
