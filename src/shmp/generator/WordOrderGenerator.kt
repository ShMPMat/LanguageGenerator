package shmp.generator

import shmp.language.syntax.*
import shmp.language.syntax.clause.translation.*
import shmp.language.syntax.features.CopulaType
import shmp.language.syntax.orderer.Orderer
import shmp.language.syntax.orderer.RelationOrderer
import shmp.language.syntax.orderer.UndefinedOrderer
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.random.Random


class WordOrderGenerator(val random: Random) {
    internal fun generateWordOrder(syntaxParadigm: SyntaxParadigm): WordOrder {
        val sovOrder = generateSovOrder()
        val copulaOrder = generateCopulaOrder(syntaxParadigm, sovOrder)
        val nominalGroupOrder = randomElement(NominalGroupOrder.values(), random)

        return WordOrder(sovOrder, copulaOrder, nominalGroupOrder)
    }

    private fun generateCopulaOrder(
        syntaxParadigm: SyntaxParadigm,
        sovOrder: Map<VerbSentenceType, SovOrder>
    ): Map<CopulaSentenceType, Orderer> {
        val result = mutableMapOf<CopulaSentenceType, Orderer>()

        CopulaSentenceType.values().forEach {
            result[it] = UndefinedOrderer
        }

        when {
            syntaxParadigm.copulaPresence.copulaType.any { it.feature == CopulaType.Verb } -> {
                CopulaSentenceType.values().forEach {
                    result[it] =
                        if (testProbability(differentCopulaWordOrderProbability(it), random))
                            RelationOrderer(generateSimpleSovOrder(), Random(random.nextLong()))
                        else RelationOrderer(sovOrder.getValue(VerbSentenceType.MainVerbClause), Random(random.nextLong()))
                }
            }
            syntaxParadigm.copulaPresence.copulaType.any { it.feature == CopulaType.Particle } -> {
                TODO()
            }
            else -> throw GeneratorException("Unknown copula type configuration")
        }

        return result
    }

    private fun generateSovOrder(): Map<VerbSentenceType, SovOrder> {
        val mainOrder = generateSimpleSovOrder()
        val resultMap = mutableMapOf(VerbSentenceType.MainVerbClause to mainOrder)

        fun writeSentenceType(sentenceType: VerbSentenceType, order: SovOrder) {
            resultMap[sentenceType] = order

            sentenceOrderPropagation[sentenceType]
                ?.filterIsInstance<VerbSentenceType>()
                ?.forEach {
                    writeSentenceType(it, order)
                }
        }

        for (sentenceType in VerbSentenceType.values().filter { it != VerbSentenceType.MainVerbClause }) {
            val order =
                if (testProbability(differentWordOrderProbability(sentenceType), random))
                    generateSimpleSovOrder()
                else mainOrder

            writeSentenceType(sentenceType, order)
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

private val sentenceOrderPropagation = mapOf<SentenceType, List<SentenceType>>(
    VerbSentenceType.QuestionVerbClause to listOf(CopulaSentenceType.QuestionCopulaClause),
    CopulaSentenceType.MainCopulaClause to listOf(CopulaSentenceType.QuestionCopulaClause)
)
