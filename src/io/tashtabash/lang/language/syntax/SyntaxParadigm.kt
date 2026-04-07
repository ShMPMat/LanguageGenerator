package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.features.CopulaPresence
import io.tashtabash.lang.language.syntax.features.PredicatePossessionPresence
import io.tashtabash.lang.language.syntax.features.QuestionMarkerPresence


data class SyntaxParadigm(
    val copula: CopulaPresence,
    val questionMarker: QuestionMarkerPresence,
    val predicatePossession: PredicatePossessionPresence
) {
    override fun toString() = """
        |Copula: $copula
        |
        |Question marker: $questionMarker
        |
        |Predicate possession: $predicatePossession
        |""".trimMargin()
}
