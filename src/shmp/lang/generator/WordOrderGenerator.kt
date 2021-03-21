package shmp.lang.generator

import shmp.lang.language.syntax.*
import shmp.lang.language.syntax.arranger.Arranger
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.lang.language.syntax.clause.translation.*
import shmp.lang.language.syntax.features.CopulaType
import shmp.random.randomSublist
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.testProbability
import kotlin.random.Random


class WordOrderGenerator(val random: Random) {
    internal fun generateWordOrder(syntaxParadigm: SyntaxParadigm): WordOrder {
        val sovOrder = generateSovOrder()
        val nominalGroupOrder = NominalGroupOrder.values().randomElement()
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
                        result[CopulaWordOrder(type, CopulaType.Verb)] = generateCopulaVerbOrderer(type, sovOrder, false)
                    }
                }
                CopulaType.Particle -> {
                    for (type in CopulaSentenceType.values()) {
                        val externalOrder = generateCopulaVerbOrderer(type, sovOrder)
                            .relationOrder
                            .referenceOrder(random)
                            .map {
                                when (it) {
                                    SyntaxRelation.Verb -> SyntaxRelation.CopulaParticle
                                    SyntaxRelation.Object -> SyntaxRelation.SubjectCompliment
                                    else -> it
                                }
                            }
                        result[CopulaWordOrder(type, CopulaType.Particle)] =
                            RelationArranger(NestedOrder(
                                StaticOrder(externalOrder),
                                nominalGroupOrder,
                                SyntaxRelation.Subject
                            ))
                    }
                }
                CopulaType.None -> {
                    for (type in CopulaSentenceType.values()) {
                        val externalOrder = generateCopulaVerbOrderer(type, sovOrder)
                            .relationOrder
                            .referenceOrder(random)
                            .filter { it != SyntaxRelation.Verb }
                        result[CopulaWordOrder(type, CopulaType.None)] =
                            RelationArranger(NestedOrder(
                                StaticOrder(externalOrder),
                                nominalGroupOrder,
                                SyntaxRelation.Subject
                            ))
                    }
                }
            }

        return result
    }

    private fun generateCopulaVerbOrderer(
        type: CopulaSentenceType,
        sovOrder: Map<VerbSentenceType, SovOrder>,
        swapObject: Boolean = true
    ): RelationArranger {
        val newOrderer = differentCopulaWordOrderProbability(type).chanceOf<RelationArranger> {
            RelationArranger(generateSimpleSovOrder())
        } ?: RelationArranger(sovOrder.getValue(VerbSentenceType.MainVerbClause))

        return RelationArranger(SubstitutingOrder(newOrderer.relationOrder) { lst, _ ->
            if (swapObject)
            lst.map { r ->
                if (r == SyntaxRelation.Object)
                    SyntaxRelation.SubjectCompliment
                else r
            }
            else lst
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
        val basicTemplate = BasicSovOrder.values().randomElement()

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