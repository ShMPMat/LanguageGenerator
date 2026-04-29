package io.tashtabash.lang.generator

import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import io.tashtabash.lang.language.syntax.clause.description.AdjunctType
import io.tashtabash.lang.language.syntax.clause.syntax.*
import io.tashtabash.lang.utils.MapWithDefault
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.withProb


class WordOrderGenerator {
    internal fun generateWordOrder(syntaxParadigm: SyntaxParadigm): WordOrder {
        val sovOrder = generateSimpleSovOrder(syntaxParadigm)
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
        sovOrder: RandomOrder,
        nominalGroupOrder: NominalGroupOrder
    ): Map<CopulaConstruction, MapWithDefault<CopulaSentenceType, Arranger>> {
        val result = mutableMapOf<CopulaConstruction, MapWithDefault<CopulaSentenceType, Arranger>>()

        for (copulaType in syntaxParadigm.copula.copula.map { it.value })
            when (copulaType) {
                CopulaConstruction.Verb -> {
                    result[CopulaConstruction.Verb] = MapWithDefault(createDefaultCopulaOrder(sovOrder))

                    for (type in CopulaSentenceType.entries)
                        createDivergingCopulaVerbOrderer(type, syntaxParadigm)?.let {
                            result.getValue(CopulaConstruction.Verb)[type] = it
                        }
                }
                CopulaConstruction.Particle -> {
                    result[CopulaConstruction.Particle] = MapWithDefault(
                        createParticleCopulaOrder(createDefaultCopulaOrder(sovOrder))
                    )

                    for (type in CopulaSentenceType.entries)
                        createDivergingCopulaVerbOrderer(type, syntaxParadigm)?.let {
                            result.getValue(CopulaConstruction.Particle)[type] = createParticleCopulaOrder(it)
                        }
                }
                CopulaConstruction.None -> {
                    result[CopulaConstruction.None] = MapWithDefault(
                        swapCopulaObject(createNoCopulaOrder(createDefaultCopulaOrder(sovOrder), nominalGroupOrder))
                    )

                    for (type in CopulaSentenceType.entries)
                        createDivergingCopulaVerbOrderer(type, syntaxParadigm)
                            ?.let {
                                result.getValue(CopulaConstruction.None)[type] = swapCopulaObject(
                                    createNoCopulaOrder(it, nominalGroupOrder)
                                )
                            }
                }
            }

        return result
    }

    private fun createParticleCopulaOrder(it: RelationArranger) = RelationArranger(
        SubstitutingOrder(it.relationOrder, mapOf(Predicate to CopulaParticle, Patient to SubjectCompliment))
    )

    private fun createNoCopulaOrder(
        arranger: RelationArranger,
        nominalGroupOrder: NominalGroupOrder
    ) = RelationArranger(
        NestedOrder(
            StaticOrder(
                arranger.relationOrder
                    .chooseReferenceOrder()
                    .filter { it != Predicate }
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

    private fun createDefaultCopulaOrder(sovOrder: RandomOrder): RelationArranger =
        RelationArranger(sovOrder)

    private fun swapCopulaObject(arranger: RelationArranger) = RelationArranger(
        SubstitutingOrder(arranger.relationOrder, mapOf(Patient to SubjectCompliment))
    )

    /**
     * excludeName is used because I filter only during the additional word order creation, and the main order
     * is expected to be created by this fun, meaning that it would contain the orders comprising it in its name.
     */
    fun generateSimpleSovOrder(syntaxParadigm: SyntaxParadigm, excludeName: String = ""): RandomOrder {
        val basicTemplate = BasicSovOrder.entries.filter { it.name !in excludeName }.randomElement()

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

        return RandomOrder(injectAdditionalRelations(references, syntaxParadigm), name)
    }

    private fun injectAdditionalRelations(
        references: List<GenericSSO<SyntaxRelations>>,
        syntaxParadigm: SyntaxParadigm
    ): List<GenericSSO<SyntaxRelations>> {
        val withQa = injectQuestionMarker(references, syntaxParadigm)
        // All cases + Manner
        val orderedObjects = (AdjunctType.entries.map { it.relation } + Manner).shuffled(RandomSingleton.random)

        return insertAtRandom(withQa, orderedObjects)
    }

    private fun injectQuestionMarker(
        references: List<GenericSSO<SyntaxRelations>>,
        syntaxParadigm: SyntaxParadigm
    ): List<GenericSSO<SyntaxRelations>> {
        syntaxParadigm.questionMarker.questionMarker
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
