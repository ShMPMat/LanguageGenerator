package io.tashtabash.lang.generator

import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.description.AdjunctType
import io.tashtabash.lang.language.syntax.clause.syntax.*
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.utils.MapWithDefault
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.withProb


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
                    result[CopulaType.Verb] = MapWithDefault(createDefaultCopulaOrder(sovOrder))

                    for (type in CopulaSentenceType.entries)
                        createDivergingCopulaVerbOrderer(type, syntaxParadigm)?.let {
                            result.getValue(CopulaType.Verb)[type] = it
                        }
                }
                CopulaType.Particle -> {
                    result[CopulaType.Particle] = MapWithDefault(
                        createParticleCopulaOrder(createDefaultCopulaOrder(sovOrder))
                    )

                    for (type in CopulaSentenceType.entries)
                        createDivergingCopulaVerbOrderer(type, syntaxParadigm)?.let {
                            result.getValue(CopulaType.Particle)[type] = createParticleCopulaOrder(it)
                        }
                }
                CopulaType.None -> {
                    result[CopulaType.None] = MapWithDefault(
                        swapCopulaObject(createNoCopulaOrder(createDefaultCopulaOrder(sovOrder), nominalGroupOrder))
                    )

                    for (type in CopulaSentenceType.entries)
                        createDivergingCopulaVerbOrderer(type, syntaxParadigm)
                            ?.let {
                                result.getValue(CopulaType.None)[type] = swapCopulaObject(
                                    createNoCopulaOrder(it, nominalGroupOrder)
                                )
                            }
                }
            }

        return result
    }

    private fun createParticleCopulaOrder(it: RelationArranger) = RelationArranger(
        SubstitutingOrder(it.relationOrder, mapOf(Verb to CopulaParticle, Patient to SubjectCompliment))
    )

    private fun createNoCopulaOrder(
        arranger: RelationArranger,
        nominalGroupOrder: NominalGroupOrder
    ) = RelationArranger(
        NestedOrder(
            StaticOrder(
                arranger.relationOrder
                    .chooseReferenceOrder()
                    .filter { it != Verb }
            ),
            nominalGroupOrder,// nominalGroupOrder is needed because the head of the sentence is a noun phrase
            Agent
        )
    )

    private fun createDivergingCopulaVerbOrderer(
        type: CopulaSentenceType,
        syntaxParadigm: SyntaxParadigm
    ): RelationArranger? =
        differentCopulaWordOrderProbability(type).chanceOf<RelationArranger> {
            RelationArranger(generateSimpleSovOrder(syntaxParadigm))
        }

    private fun createDefaultCopulaOrder(sovOrder: Map<VerbSentenceType, SovOrder>): RelationArranger =
        RelationArranger(sovOrder.getValue(VerbSentenceType.MainVerbClause))

    private fun swapCopulaObject(arranger: RelationArranger) = RelationArranger(
        SubstitutingOrder(arranger.relationOrder, mapOf(Patient to SubjectCompliment))
    )

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
        val orderedObjects = AdjunctType.entries.shuffled(RandomSingleton.random)

        return insertAtRandom(withQa, orderedObjects.map { it.relation })
    }

    private fun injectQuestionMarker(
        references: List<GenericSSO<SyntaxRelations>>,
        syntaxParadigm: SyntaxParadigm
    ): List<GenericSSO<SyntaxRelations>> {
        syntaxParadigm.questionMarkerPresence.questionMarker
            ?: return references

        return insertAtRandom(references, listOf(QuestionMarker))
    }

    private fun insertAtRandom(
        references: List<GenericSSO<SyntaxRelations>>,
        elements: SyntaxRelations
    ): List<GenericSSO<SyntaxRelations>> {
        val position = RandomSingleton.random.nextInt(references.size + 1)

        return references.map {
            (it.value.take(position) + elements + it.value.drop(position))
                .withProb(it.probability)
        }
    }
}

private val sentenceOrderPropagation = mapOf<SentenceType, List<SentenceType>>(
    VerbSentenceType.QuestionVerbClause to listOf(CopulaSentenceType.QuestionCopulaClause),
    CopulaSentenceType.MainCopulaClause to listOf(CopulaSentenceType.QuestionCopulaClause)
)
