package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.randomUnwrappedElement
import io.tashtabash.random.withProb


interface RelationOrder {
    val references: List<GenericSSO<SyntaxRelations>>

    fun chooseReferenceOrder(): SyntaxRelations =
        references.randomUnwrappedElement()
}


class SubstitutingOrder(
    val relationOrder: RelationOrder,
    val substitutions: Map<SyntaxRelation, SyntaxRelation>
): RelationOrder {
    override val references: List<GenericSSO<List<SyntaxRelation>>>
        get() = relationOrder.references
            .map {
                it.value
                    .map { r -> substitutions.getOrDefault(r, r) }
                    .withProb(it.probability)
            }

    override fun toString() = references.joinToString { it.value.toString() }
}


class NestedOrder(
    val outerOrder: RelationOrder,
    val innerOrder: RelationOrder,
    val nestedRelation: SyntaxRelation
): RelationOrder {
    override val references = outerOrder.references.flatMap { outerOrder ->
        innerOrder.references.map { innerOrder ->
            (outerOrder.value.takeWhile { it != nestedRelation } +
                    innerOrder.value +
                    outerOrder.value.takeLastWhile { it != nestedRelation })
                .withProb(outerOrder.probability * innerOrder.probability)
        }
    }

    override fun toString() = "Order by $innerOrder as $nestedRelation and then order by $outerOrder"
}

class StaticOrder(val order: List<SyntaxRelation>): RelationOrder {
    override val references = listOf(order.withProb(1.0))

    override fun toString() = "Order of " + order.joinToString(", ")
}

class RandomOrder(override val references: List<GenericSSO<SyntaxRelations>>, val name: String = "?") : RelationOrder {
    override fun toString() = name
}


enum class BasicSovOrder(
    override val references: List<GenericSSO<SyntaxRelations>>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    SOV(listOf(listOf(Agent, Patient, Predicate).withProb(1.0)), 565.0),
    SVO(listOf(listOf(Agent, Predicate, Patient).withProb(1.0)), 488.0),
    VSO(listOf(listOf(Predicate, Agent, Patient).withProb(1.0)), 95.0),
    VOS(listOf(listOf(Predicate, Patient, Agent).withProb(1.0)), 25.0),
    OVS(listOf(listOf(Patient, Predicate, Agent).withProb(1.0)), 11.0),
    OSV(listOf(listOf(Patient, Agent, Predicate).withProb(1.0)), 4.0),
    Two(listOf(), 67.0),
    None(listOf(SOV, SVO, VSO, VOS, OVS, OSV).flatMap { it.references }, 122.0)
}

enum class NominalGroupOrder(
    override val references: List<GenericSSO<SyntaxRelations>>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder { // I haven't found any info about the probabilities
    DNP(listOf(listOf(Definition, Nominal, Possessor).withProb(1.0)), 10.0),
    NDP(listOf(listOf(Nominal, Definition, Possessor).withProb(1.0)), 100.0),
    DPN(listOf(listOf(Definition, Possessor, Nominal).withProb(1.0)), 100.0),
    NPD(listOf(listOf(Nominal, Possessor, Definition).withProb(1.0)), 100.0),
    PDN(listOf(listOf(Possessor, Definition, Nominal).withProb(1.0)), 100.0),
    PND(listOf(listOf(Possessor, Nominal, Definition).withProb(1.0)), 10.0),
    None(listOf(DNP, NDP, DPN, NPD, PDN, PND).flatMap { it.references }, 10.0)
}
