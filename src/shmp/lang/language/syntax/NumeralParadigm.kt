package shmp.lang.language.syntax

import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.Lexis
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.syntax.arranger.Arranger
import shmp.lang.language.syntax.clause.realization.wordToNode
import shmp.lang.language.syntax.clause.translation.SentenceNode


data class NumeralParadigm(val base: NumeralSystemBase, val ranges: NumeralRanges) {
    fun constructNumeral(n: Int, lexis: Lexis): SentenceNode {
        if (n < 1)
            throw SyntaxException("No numeral for $n")

        return extract(n, lexis).apply { parentPropagation = true }
    }

    private fun extract(n: Int, lexis: Lexis): SentenceNode =
        when (val type = ranges.first { (r) -> r.contains(n) }.second) {
            NumeralConstructionType.SingleWord -> lexis.getWord(n.toString()).wordToNode(SyntaxRelation.AdNumeral)
            is NumeralConstructionType.SpecialWord -> lexis.getWord(type.meaning).wordToNode(SyntaxRelation.AdNumeral)
            is NumeralConstructionType.AddWord -> {
                val baseNumber = n / type.baseNumber
                val sumNumber = n % type.baseNumber

                val baseNode = constructBaseNode(baseNumber, type.baseNumber, lexis)
                val sumNode = extract(sumNumber, lexis)
                baseNode.addStrayChild(SyntaxRelation.SumNumeral, sumNode)
                baseNode.arranger = type.arranger

                baseNode
            }
        }

    private fun constructBaseNode(mulNumber: Int, baseNumber: Int, lexis: Lexis): SentenceNode {
        val baseNode = extract(baseNumber, lexis)
        val mulNode = extract(mulNumber, lexis)
        if (mulNumber != 1)
            baseNode.addStrayChild(SyntaxRelation.MulNumeral, mulNode)

        return baseNode
    }

    override fun toString() = """
         |Numeral system base: $base
         |""".trimMargin()
}

sealed class NumeralConstructionType {
    object SingleWord : NumeralConstructionType()
    data class SpecialWord(val meaning: Meaning) : NumeralConstructionType()
    data class AddWord(val arranger: Arranger, val baseNumber: Int) : NumeralConstructionType()
}

typealias NumeralRanges = List<Pair<IntRange, NumeralConstructionType>>
