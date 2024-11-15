package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.generator.util.GeneratorException
import io.tashtabash.lang.language.lexis.AbstractLexis
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.lexis.WordPointer
import io.tashtabash.lang.language.lineUp


interface ChangeHistory {
    fun printHistory(parent: Word, lexis: AbstractLexis): String

    fun computeChangeDepth(lexis: AbstractLexis): Int
}


data class DerivationHistory(val derivation: Derivation, val previous: WordPointer) : ChangeHistory {
    override fun printHistory(parent: Word, lexis: AbstractLexis): String =
        constructChangeTree(listOf(previous.resolve(lexis)), parent, lexis, derivation.derivationClass.toString())

    override fun computeChangeDepth(lexis: AbstractLexis): Int {
        val previousDepth = previous.resolve(lexis)
            .semanticsCore
            .changeHistory
            ?.computeChangeDepth(lexis)
            ?: 0

        return 1 + previousDepth
    }
}


data class CompoundHistory(val compound: Compound, val previous: List<WordPointer>) : ChangeHistory {
    override fun printHistory(parent: Word, lexis: AbstractLexis): String =
        constructChangeTree(previous.map { it.resolve(lexis) }, parent, lexis, compound.infix.toString())

    override fun computeChangeDepth(lexis: AbstractLexis): Int {
        val previousDepth = previous.mapNotNull {
            it.resolve(lexis)
                .semanticsCore
                .changeHistory
                ?.computeChangeDepth(lexis)
        }.maxOrNull()
            ?: 0

        return 1 + previousDepth
    }
}


private fun constructChangeTree(previousWords: List<Word>, parentWord: Word, lexis: AbstractLexis, arrowLabel: String): String {
    val (maxDepthString, maxDepth) = previousWords.maxByOrNull { it.semanticsCore.computeChangeDepth(lexis) }
        ?.let { it.printChange(lexis) to it.semanticsCore.computeChangeDepth(lexis) }
        ?: throw GeneratorException("Empty word list has been given")
    val indexes = getIndexes(maxDepthString)
    val prefix = previousWords
        .map { it.semanticsCore.computeChangeDepth(lexis) to it.printChange(lexis) }
        .joinToString("\n") { (d, s) ->
            s.lines().joinToString("\n") {
                " ".repeat(indexes.elementAtOrNull(maxDepth - d) ?: indexes.last()) + it
            }
        }

    val prefixWithArrows = prefix.lines().lineUp()
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

        indexes += index + 3
    }

    return indexes
}

private fun Word.printChange(lexis: AbstractLexis) = semanticsCore.changeHistory?.printHistory(this, lexis)
    ?: lineUp(toString(), semanticsCore.toString()).let { (f, s) -> "$f\n$s" }
