package shmp.lang.language.derivation

import shmp.lang.generator.util.GeneratorException
import shmp.lang.language.lexis.Word
import shmp.lang.language.lineUp


interface ChangeHistory {
    fun printHistory(parent: Word): String

    val changeDepth: Int
}


data class DerivationHistory(val derivation: Derivation, val previous: Word) : ChangeHistory {
    override fun printHistory(parent: Word) =
        constructChangeTree(listOf(previous), parent, derivation.dClass.toString())

    override val changeDepth: Int
        get() = 1 + (previous.semanticsCore.changeHistory?.changeDepth ?: 0)
}


data class CompoundHistory(val compound: Compound, val previous: List<Word>) : ChangeHistory {
    override fun printHistory(parent: Word) =
        constructChangeTree(previous, parent, compound.infix.toString())

    override val changeDepth: Int
        get() = 1 + (previous.map { it.semanticsCore.changeHistory?.changeDepth ?: 0 }.max() ?: 0)
}


private fun constructChangeTree(previousWords: List<Word>, parentWord: Word, arrowLabel: String): String {
    val (maxDepthString, maxDepth) = previousWords.maxByOrNull { it.semanticsCore.changeDepth }
        ?.let { it.printChange() to it.semanticsCore.changeDepth }
        ?: throw GeneratorException("Empty word list has been given")
    val indexes = getIndexes(maxDepthString)
    val prefix = previousWords
        .map { it.semanticsCore.changeDepth to it.printChange() }
        .joinToString("\n") { (d, s) ->
            s.lines().joinToString("\n") {
                " ".repeat(indexes.elementAtOrNull(maxDepth - d) ?: indexes.last()) + it
            }
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

private fun getIndexes(maxDepthString: String): List<Int> {
    val (oneLine) = maxDepthString.lines()
    val indexes = mutableListOf(0)
    var index = 0

    while (true) {
        index = oneLine.indexOf("->", index + 1)
        index = oneLine.indexOf("->", index + 1)

        if (index == -1)
            break

        indexes.add(index + 3)
    }

    return indexes
}

private fun Word.printChange() = semanticsCore.changeHistory?.printHistory(this)
    ?: lineUp(toString(), semanticsCore.toString()).let { (f, s) -> "$f\n$s" }
