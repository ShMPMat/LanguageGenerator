package shmp.lang.language.syntax.numeral

import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.Lexis
import shmp.lang.language.lineUpAll
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.numeral.NumeralConstructionType.*
import shmp.lang.language.syntax.clause.realization.wordToNode
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.random.singleton.testProbability


data class NumeralParadigm(val base: NumeralSystemBase, val ranges: NumeralRanges) {
    fun constructNumeral(n: Int, lexis: Lexis): SentenceNode {
        if (n < 1)
            throw SyntaxException("No numeral for $n")

        return extract(n, lexis).apply { parentPropagation = true }
    }

    private fun extract(n: Int, lexis: Lexis): SentenceNode =
        when (val type = getType(n)) {
            SingleWord -> lexis.getWord(n.toString()).wordToNode(SyntaxRelation.AdNumeral)
            is SpecialWord -> lexis.getWord(type.meaning).wordToNode(SyntaxRelation.AdNumeral)
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
