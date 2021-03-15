package shmp.language.syntax

import shmp.language.syntax.features.CopulaPresence


data class SyntaxParadigm(val copulaPresence: CopulaPresence) {
    override fun toString(): String {
        return """
            |
            |Copula: $copulaPresence
            |
            |""".trimMargin()
    }
}
