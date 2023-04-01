package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.features.CopulaPresence
import io.tashtabash.lang.language.syntax.features.PredicatePossessionPresence
import io.tashtabash.lang.language.syntax.features.QuestionMarkerPresence


data class SyntaxParadigm(
    val copulaPresence: CopulaPresence,
    val questionMarkerPresence: QuestionMarkerPresence,
    val predicatePossessionPresence: PredicatePossessionPresence
) {
    override fun toString() = """
        |Copula: $copulaPresence
        |
        |Question marker: $questionMarkerPresence
        |
        |Predicate possession: $predicatePossessionPresence
        |""".trimMargin()
}
