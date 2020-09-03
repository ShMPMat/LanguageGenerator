package shmp.language.derivation

import shmp.language.lexis.Word
import shmp.language.lineUp


interface ChangeHistory {
    fun printHistory(parent: Word): String

    val derivationDepth: Int
}


data class DerivationHistory(val derivation: Derivation, val previous: Word): ChangeHistory {
    override fun printHistory(parent: Word) =
        constructChangeTree(listOf(previous), parent, derivation.dClass.toString())

    override val derivationDepth: Int
        get() = 1 + (previous.semanticsCore.changeHistory?.derivationDepth ?: 0)
}


data class CompoundHistory(val compound: Compound, val previous: List<Word>): ChangeHistory {
    override fun printHistory(parent: Word) =
        constructChangeTree(previous, parent, compound.infix.toString())

    override val derivationDepth: Int
        get() = 1 + (previous.map { it.semanticsCore.changeHistory?.derivationDepth ?: 0 }.max() ?: 0 )
}


private fun constructChangeTree(previousWords: List<Word>, parentWord: Word, arrowLabel: String): String {
    val prefix =
        previousWords.joinToString("\n") {
            it.semanticsCore.changeHistory?.printHistory(it)
                ?: lineUp(it.toString(), it.semanticsCore.toString()).let { (f, s) -> "$f\n$s" }
        }

    val prefixWithArrows = lineUp(prefix.lines())
        .joinToString("\n") { "$it ->$arrowLabel-> " }
        .lines()

    val (lanPrefix, commentPrefix) = prefixWithArrows.takeLast(2)
    val (lanPostfix, commentPostfix) = lineUp(parentWord.toString(), parentWord.semanticsCore.toString())

    return prefixWithArrows.dropLast(2).joinToString("") { "$it\n" } +
            lanPrefix + lanPostfix + "\n" +
            commentPrefix + commentPostfix
}
