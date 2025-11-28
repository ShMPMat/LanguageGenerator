package io.tashtabash.lang.generator

import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.description.AdjunctType
import io.tashtabash.lang.language.syntax.clause.translation.*
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.utils.MapWithDefault
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.toSampleSpaceObject


class WordOrderGenerator {
    internal fun generateWordOrder(syntaxParadigm: SyntaxParadigm): WordOrder {
        val sovOrder = generateSovOrder(syntaxParadigm)
        val nominalGroupOrder = NominalGroupOrder.entries.randomElement()
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
    ): Map<CopulaType, MapWithDefault<CopulaSentenceType, Arranger>> {
        val result = mutableMapOf<CopulaType, MapWithDefault<CopulaSentenceType, Arranger>>()

        for (copulaType in syntaxParadigm.copulaPresence.copulaType.map { it.feature })
            when (copulaType) {
                CopulaType.Verb -> {
                    result[CopulaType.Verb] = MapWithDefault(createDefaultCopulaOrder(sovOrder, false))

                    for (type in CopulaSentenceType.entries)
                        generateCopulaVerbOrderer(type, syntaxParadigm, false)?.let {
                            result.getValue(CopulaType.Verb)[type] = it
                        }
                }
                CopulaType.Particle -> {
                    result[CopulaType.Particle] = createDefaultOrder    (sovOrder, nominalGroupOrder)

                    for (type in CopulaSentenceType.entries)
                        generateCopulaVerbOrderer(type, syntaxParadigm)
                            ?.relationOrder
                            ?.chooseReferenceOrder
                            ?.map {
                                when (it) {
                                    SyntaxRelation.Verb -> SyntaxRelation.CopulaParticle
                                    SyntaxRelation.Patient -> SyntaxRelation.SubjectCompliment
                                    else -> it
                                }
                            }?.let {
                                result.getValue(CopulaType.Particle)[type] = wrapInNested(it, nominalGroupOrder)
                            }
                }
                CopulaType.None -> {
                    result[CopulaType.None] = createDefaultOrder(sovOrder, nominalGroupOrder)

                    for (type in CopulaSentenceType.entries)
                        generateCopulaVerbOrderer(type, syntaxParadigm)
                            ?.relationOrder
                            ?.chooseReferenceOrder
                            ?.filter { it != SyntaxRelation.Verb }
                            ?.let {
                                result.getValue(CopulaType.None)[type] = wrapInNested(it, nominalGroupOrder)
                            }
                }
            }

        return result
    }

    private fun createDefaultOrder(
        sovOrder: Map<VerbSentenceType, SovOrder>,
        nominalGroupOrder: NominalGroupOrder
    ): MapWithDefault<CopulaSentenceType, Arranger> =
        MapWithDefault(
            wrapInNested(
                createDefaultCopulaOrder(sovOrder).relationOrder.chooseReferenceOrder,
                nominalGroupOrder
            )
        )

    private fun wrapInNested(syntaxRelations: SyntaxRelations, nominalGroupOrder: NominalGroupOrder) =
        RelationArranger(NestedOrder(
            StaticOrder(syntaxRelations),
            nominalGroupOrder,
            SyntaxRelation.Agent
        ))

    private fun generateCopulaVerbOrderer(
        type: CopulaSentenceType,
        syntaxParadigm: SyntaxParadigm,
        swapObject: Boolean = true
    ): RelationArranger? {
        val newArranger = differentCopulaWordOrderProbability(type).chanceOf<RelationArranger> {
            RelationArranger(generateSimpleSovOrder(syntaxParadigm))
        } ?: return null

        if (!swapObject)
            return newArranger

        return swapObject(newArranger)
    }

    private fun createDefaultCopulaOrder(
        sovOrder: Map<VerbSentenceType, SovOrder>,
        swapObject: Boolean = true
    ): RelationArranger {
        val arranger = RelationArranger(sovOrder.getValue(VerbSentenceType.MainVerbClause))

        if (!swapObject)
            return arranger

        return swapObject(arranger)
    }

    private fun swapObject(arranger: RelationArranger): RelationArranger =
        RelationArranger(SubstitutingOrder(arranger.relationOrder) { lst ->
            lst.map { r ->
                if (r == SyntaxRelation.Patient)
                    SyntaxRelation.SubjectCompliment
                else r
            }
        })

    private fun generateSovOrder(syntaxParadigm: SyntaxParadigm): MapWithDefault<VerbSentenceType, SovOrder> {
        val mainOrder = generateSimpleSovOrder(syntaxParadigm)
        val exceptions = mutableMapOf<VerbSentenceType, SovOrder>()

        fun writeSentenceType(sentenceType: VerbSentenceType, order: SovOrder) {
            exceptions[sentenceType] = order

            sentenceOrderPropagation[sentenceType]
                ?.filterIsInstance<VerbSentenceType>()
                ?.forEach {
                    writeSentenceType(it, order)
                }
        }

        for (sentenceType in VerbSentenceType.entries.filter { it != VerbSentenceType.MainVerbClause })
            differentWordOrderProbability(sentenceType).chanceOf {
                val differentOrder = generateSimpleSovOrder(syntaxParadigm)
                writeSentenceType(sentenceType, differentOrder)
            }

        return MapWithDefault(mainOrder, exceptions)
    }

    private fun generateSimpleSovOrder(syntaxParadigm: SyntaxParadigm): SovOrder {
        val basicTemplate = BasicSovOrder.entries.randomElement()

        val (references, name) = when (basicTemplate) {
            BasicSovOrder.Two -> {
                val (t1, t2) = randomSublist(
                    BasicSovOrder.entries.take(6),
                    { it.probability },
                    RandomSingleton.random,
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
        val orderedObjects = AdjunctType.entries.shuffled(RandomSingleton.random)

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
            (it.value.take(position) + SyntaxRelation.QuestionMarker + it.value.drop(position))
                .toSampleSpaceObject(it.probability)
        }
    }
}

private val sentenceOrderPropagation = mapOf<SentenceType, List<SentenceType>>(
    VerbSentenceType.QuestionVerbClause to listOf(CopulaSentenceType.QuestionCopulaClause),
    CopulaSentenceType.MainCopulaClause to listOf(CopulaSentenceType.QuestionCopulaClause)
)
