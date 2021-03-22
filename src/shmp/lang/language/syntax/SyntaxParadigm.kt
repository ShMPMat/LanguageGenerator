package shmp.lang.language.syntax

import shmp.lang.language.syntax.features.CopulaPresence


data class SyntaxParadigm(val copulaPresence: CopulaPresence, /*prodrop*/) {
    override fun toString() = """
        |
        |Copula: $copulaPresence
        |
        |""".trimMargin()
}
