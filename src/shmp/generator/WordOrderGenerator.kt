package shmp.generator

import shmp.language.syntax.BasicSovOrder
import shmp.language.syntax.NominalGroupOrder
import shmp.language.syntax.SovOrder
import shmp.language.syntax.WordOrder
import shmp.language.syntax.clause.translation.SentenceType
import shmp.language.syntax.clause.translation.differentWordOrderProbability
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.random.Random


class WordOrderGenerator(val random: Random) {
    internal fun generateWordOrder(): WordOrder {
        val sovOrder = generateSovOrder()
        val nominalGroupOrder = randomElement(NominalGroupOrder.values(), random)

        return WordOrder(sovOrder, nominalGroupOrder)
    }

    private fun generateSovOrder(): Map<SentenceType, SovOrder> {
        val mainOrder = generateSimpleSovOrder()
        val resultMap = mutableMapOf(SentenceType.MainVerbClause to mainOrder)

        for (sentenceType in SentenceType.values().filter { it != SentenceType.MainVerbClause }) {
            resultMap[sentenceType] =
                if (testProbability(differentWordOrderProbability(sentenceType), random))
                    generateSimpleSovOrder()
                else mainOrder
        }

        return resultMap
    }

    private fun generateSimpleSovOrder(): SovOrder {
        val basicTemplate = randomElement(BasicSovOrder.values(), random)

        val (references, name) = when (basicTemplate) {
            BasicSovOrder.Two -> {
                val (t1, t2) = randomSublist(
                    BasicSovOrder.values().take(6),
                    { it.probability },
                    random,
                    2,
                    3
                )
                val referenceOrder = ({ r: Random ->
                    (if (r.nextBoolean()) t1 else t2).referenceOrder(r)
                })
                referenceOrder to "$t1 or $t2"
            }
            else -> basicTemplate.referenceOrder to basicTemplate.name
        }

        return SovOrder(references, name)
    }
}
