package shmp.lang.language.syntax

import shmp.lang.language.syntax.features.CopulaPresence
import shmp.lang.language.syntax.features.PossessionConstructionPresence
import shmp.lang.language.syntax.features.QuestionMarkerPresence


data class SyntaxParadigm(
    val copulaPresence: CopulaPresence,
    val questionMarkerPresence: QuestionMarkerPresence,
    val possessionConstructionPresence: PossessionConstructionPresence
) {
    override fun toString() = """
        |Copula: $copulaPresence
        |
        |Question marker: $questionMarkerPresence
        |
        |Possession construction: $possessionConstructionPresence
        |""".trimMargin()
}
