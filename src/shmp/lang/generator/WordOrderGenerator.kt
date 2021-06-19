package shmp.lang.generator

import shmp.lang.language.syntax.*
import shmp.lang.language.syntax.arranger.Arranger
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.lang.language.syntax.clause.description.IndirectObjectType
import shmp.lang.language.syntax.clause.translation.*
import shmp.lang.language.syntax.features.CopulaType
import shmp.random.GenericSSO
import shmp.random.randomSublist
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.toSampleSpaceObject
import kotlin.random.Random


class WordOrderGenerator(val random: Random) {
    internal fun generateWordOrder(syntaxParadigm: SyntaxParadigm): WordOrder {
        val sovOrder = generateSovOrder(syntaxParadigm)
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
                        result[CopulaWordOrder(type, CopulaType.Verb)] =
                            generateCopulaVerbOrderer(type, sovOrder, syntaxParadigm, false)
                    }
                }
                CopulaType.Particle -> {
                    for (type in CopulaSentenceType.values()) {
                        val externalOrder = generateCopulaVerbOrderer(type, sovOrder, syntaxParadigm)
                            .relationOrder
                            .chooseReferenceOrder
                            .map {
                                when (it) {
                                    SyntaxRelation.Verb -> SyntaxRelation.CopulaParticle
                                    SyntaxRelation.Patient -> SyntaxRelation.SubjectCompliment
                                    else -> it
                                }
                            }
                        result[CopulaWordOrder(type, CopulaType.Particle)] =
                            RelationArranger(NestedOrder(
                                StaticOrder(externalOrder),
                                nominalGroupOrder,
                                SyntaxRelation.Agent
                            ))
                    }
                }
                CopulaType.None -> {
                    for (type in CopulaSentenceType.values()) {
                        val externalOrder = generateCopulaVerbOrderer(type, sovOrder, syntaxParadigm)
                            .relationOrder
                            .chooseReferenceOrder
                            .filter { it != SyntaxRelation.Verb }
                        result[CopulaWordOrder(type, CopulaType.None)] =
                            RelationArranger(NestedOrder(
                                StaticOrder(externalOrder),
                                nominalGroupOrder,
                                SyntaxRelation.Agent
                            ))
                    }
                }
            }

        return result
    }

    private fun generateCopulaVerbOrderer(
        type: CopulaSentenceType,
        sovOrder: Map<VerbSentenceType, SovOrder>,
        syntaxParadigm: SyntaxParadigm,
        swapObject: Boolean = true
    ): RelationArranger {
        val newOrderer = differentCopulaWordOrderProbability(type).chanceOf<RelationArranger> {
            RelationArranger(generateSimpleSovOrder(syntaxParadigm))
        } ?: RelationArranger(sovOrder.getValue(VerbSentenceType.MainVerbClause))

        return RelationArranger(SubstitutingOrder(newOrderer.relationOrder) { lst ->
            if (swapObject)
            lst.map { r ->
                if (r == SyntaxRelation.Patient)
                    SyntaxRelation.SubjectCompliment
                else r
            }
            else lst
        })
    }

    private fun generateSovOrder(syntaxParadigm: SyntaxParadigm): Map<VerbSentenceType, SovOrder> {
        val mainOrder = generateSimpleSovOrder(syntaxParadigm)
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
            val order = differentWordOrderProbability(sentenceType).chanceOf<SovOrder> {
                generateSimpleSovOrder(syntaxParadigm)
            } ?: mainOrder

            writeSentenceType(sentenceType, order)
        }

        return resultMap
    }

    private fun generateSimpleSovOrder(syntaxParadigm: SyntaxParadigm): SovOrder {
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
                val references = t1.references + t2.references
                references to "$t1 or $t2"
            }
            else -> basicTemplate.references to basicTemplate.name
        }

        return SovOrder(injectAdditionalRelations(references, syntaxParadigm), name)
    }

    private fun injectAdditionalRelations(
        references: List<GenericSSO<SyntaxRelations>>,
        syntaxParadigm: SyntaxParadigm
    ): List<GenericSSO<SyntaxRelations>> {
        val withQa = injectQuestionMarker(references, syntaxParadigm)

        val position = RandomSingleton.random.nextInt(withQa.size + 1)
        val orderedObjects = IndirectObjectType.values().toList().shuffled(RandomSingleton.random)

        return withQa.map {
            (it.value.take(position) + orderedObjects.map { io -> io.relation } + it.value.drop(position))
                .toSampleSpaceObject(it.probability)
        }
    }

    private fun injectQuestionMarker(
        references: List<GenericSSO<SyntaxRelations>>,
        syntaxParadigm: SyntaxParadigm
    ): List<GenericSSO<SyntaxRelations>> {
        syntaxParadigm.questionMarkerPresence.questionMarker
            ?: return references

        val position = RandomSingleton.random.nextInt(references.size + 1)

        return references.map {
            (it.value.take(position) + listOf(SyntaxRelation.QuestionMarker) + it.value.drop(position))
                .toSampleSpaceObject(it.probability)
        }
    }
}

private val sentenceOrderPropagation = mapOf<SentenceType, List<SentenceType>>(
    VerbSentenceType.QuestionVerbClause to listOf(CopulaSentenceType.QuestionCopulaClause),
    CopulaSentenceType.MainCopulaClause to listOf(CopulaSentenceType.QuestionCopulaClause)
)
