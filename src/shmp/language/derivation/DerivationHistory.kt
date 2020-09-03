package shmp.language.derivation

import shmp.language.lexis.Word
import shmp.language.lineUp


data class DerivationHistory(val derivation: Derivation, val previous: Word) {
    fun printHistory(parent: Word): String {
        val previousHistory = previous.semanticsCore.derivationHistory

        val prefix =
            previousHistory?.printHistory(previous)
                ?: lineUp(previous.toString(), previous.semanticsCore.toString()).let { (f, s) -> "$f\n$s" }

        val prefixWithArrows = prefix
            .lines()
            .joinToString("\n") { it + " ->${derivation.dClass}-> " }
            .lines()

        val (lanPrefix, commentPrefix) = prefixWithArrows.takeLast(2)
        val (lanPostfix, commentPostfix) = lineUp(parent.toString(), parent.semanticsCore.toString())

        return prefixWithArrows.drop(2).joinToString { "$it\n" } +
                lanPrefix + lanPostfix + "\n" +
                commentPrefix + commentPostfix
    }

    val derivationDepth: Int
        get() = 1 + (previous.semanticsCore.derivationHistory?.derivationDepth ?: 0)
}
