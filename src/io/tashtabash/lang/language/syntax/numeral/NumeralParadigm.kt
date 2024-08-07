package io.tashtabash.lang.language.syntax.numeral

import io.tashtabash.lang.language.NumeralSystemBase
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lineUpAll
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.numeral.NumeralConstructionType.*
import io.tashtabash.lang.language.syntax.clause.realization.toNode
import io.tashtabash.lang.language.syntax.clause.translation.SentenceNode
import io.tashtabash.random.singleton.testProbability


data class NumeralParadigm(val base: NumeralSystemBase, val ranges: NumeralRanges) {
    fun constructNumeral(n: Int, lexis: Lexis): SentenceNode {
        if (n < 1)
            throw SyntaxException("No numeral for $n")

        return extract(n, lexis).apply { parentPropagation = true }
    }

    private fun extract(n: Int, lexis: Lexis): SentenceNode =
        when (val type = getType(n)) {
            SingleWord -> lexis.getWord(n.toString()).toNode(SyntaxRelation.AdNumeral)
            is SpecialWord -> lexis.getWord(type.meaning).toNode(SyntaxRelation.AdNumeral)
            is AddWord -> {
                val baseNumber = n / type.baseNumber
                val sumNumber = n % type.baseNumber

                val baseNode = constructBaseNode(baseNumber, type.baseNumber, type.oneProb, lexis)
                if (sumNumber != 0) {
                    val sumNode = extract(sumNumber, lexis)
                    baseNode.addStrayChild(SyntaxRelation.SumNumeral, sumNode)
                }
                baseNode.arranger = type.arranger

                baseNode
            }
        }

    private fun constructBaseNode(mulNumber: Int, baseNumber: Int, oneProb: Double, lexis: Lexis): SentenceNode {
        val type = getType(mulNumber * baseNumber)
        if (type == SingleWord || type is SpecialWord)
            return extract(mulNumber * baseNumber, lexis)

        val baseNode = extract(baseNumber, lexis)
        val mulNode = extract(mulNumber, lexis)
        if (mulNumber != 1 || oneProb.testProbability())
            baseNode.addStrayChild(SyntaxRelation.MulNumeral, mulNode)

        return baseNode
    }

    private fun getType(n: Int) = ranges.first { (r) -> r.contains(n) }.second

    override fun toString() = """
         |Numeral system base: $base
         |${
        ranges.map { (r, t) -> listOf("From ${r.first} to ${r.last} - ", t.toString()) }
            .lineUpAll()
            .joinToString("\n")
    }
         |
         |""".trimMargin()
}

typealias NumeralRanges = List<Pair<IntRange, NumeralConstructionType>>
