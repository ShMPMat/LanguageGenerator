package shmp.generator

import shmp.language.syntax.*
import shmp.language.syntax.clause.translation.*
import shmp.language.syntax.features.CopulaType
import shmp.language.syntax.arranger.Arranger
import shmp.language.syntax.arranger.RelationArranger
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.random.Random


class WordOrderGenerator(val random: Random) {
    internal fun generateWordOrder(syntaxParadigm: SyntaxParadigm): WordOrder {
        val sovOrder = generateSovOrder()
        val nominalGroupOrder = randomElement(NominalGroupOrder.values(), random)
        val copulaOrder = generateCopulaOrder(
            syntaxParadigm,
            sovOrder,
            nominalGroupOrder
        )

        return WordOrder(sovOrder, copulaOrder, nominalGroupOrder)
    }

    private fun generateCopulaOrder(
        syntaxParadigm: SyntaxParadigm,
        sovOrder: Map<VerbSentenceType, SovOrder>,
        nominalGroupOrder: NominalGroupOrder
    ): Map<CopulaWordOrder, Arranger> {
        val result = mutableMapOf<CopulaWordOrder, Arranger>()

        for (copulaType in syntaxParadigm.copulaPresence.copulaType.map { it.feature })
            when (copulaType) {
                CopulaType.Verb -> {
                    for (type in CopulaSentenceType.values()) {
                        result[CopulaWordOrder(type, CopulaType.Verb)] = generateCopulaVerbOrderer(type, sovOrder)
                    }
                }
                CopulaType.Particle -> {
                    TODO()
                }
                CopulaType.None -> {
                    for (type in CopulaSentenceType.values()) {
                        val externalOrder = generateCopulaVerbOrderer(type, sovOrder)
                        result[CopulaWordOrder(type, CopulaType.None)] =
                            RelationArranger(SubstitutingOrder(externalOrder.relationOrder) { lst, random ->
                                lst.takeWhile { it != SyntaxRelation.Subject } +
                                        nominalGroupOrder.referenceOrder(random) +
                                        lst.takeLastWhile { it != SyntaxRelation.Subject }
                            })
                    }
                }
            }

        return result
    }

    private fun generateCopulaVerbOrderer(
        type: CopulaSentenceType,
        sovOrder: Map<VerbSentenceType, SovOrder>
    ): RelationArranger {
        val newOrderer =
            if (testProbability(differentCopulaWordOrderProbability(type), random))
                RelationArranger(generateSimpleSovOrder())
            else RelationArranger(sovOrder.getValue(VerbSentenceType.MainVerbClause))

        return RelationArranger(SubstitutingOrder(newOrderer.relationOrder) { lst, _ ->
            lst.map { r ->
                if (r == SyntaxRelation.Object)
                    SyntaxRelation.SubjectCompliment
                else r
            }
        })
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
