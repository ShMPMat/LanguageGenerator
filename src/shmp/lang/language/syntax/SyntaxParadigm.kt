package shmp.lang.language.syntax

import shmp.lang.language.syntax.features.CopulaPresence


data class SyntaxParadigm(val copulaPresence: CopulaPresence) {
    override fun toString(): String {
        return """
            |
            |Copula: $copulaPresence
            |
            |""".trimMargin()
    }
}
