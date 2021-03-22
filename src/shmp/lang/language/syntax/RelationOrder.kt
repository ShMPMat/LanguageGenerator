package shmp.lang.language.syntax

import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.lang.language.syntax.clause.translation.CopulaSentenceType
import shmp.lang.language.syntax.features.CopulaType
import shmp.random.GenericSSO
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomUnwrappedElement
import shmp.random.toSampleSpaceObject


interface RelationOrder {
    val references: List<GenericSSO<SyntaxRelations>>

    val chooseReferenceOrder: SyntaxRelations
        get() = references.randomUnwrappedElement()
}


class SubstitutingOrder(
    val relationOrder: RelationOrder,
    val substituteFun: (SyntaxRelations) -> SyntaxRelations
): RelationOrder {
    override val references = relationOrder.references
        .map { substituteFun(it.value).toSampleSpaceObject(it.probability) }
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
                .toSampleSpaceObject(outerOrder.probability * innerOrder.probability)
        }
    }

    override fun toString() = "Order by $innerOrder as $nestedRelation and then order by $outerOrder"
}

class StaticOrder(
    val order: List<SyntaxRelation>
): RelationOrder {
    override val references = listOf(order.toSampleSpaceObject(1.0))

    override fun toString() = "Order of " + order.joinToString(", ")
}

class SovOrder(override val references: List<GenericSSO<SyntaxRelations>>, val name: String) : RelationOrder {

    override fun toString() = name
}

data class CopulaWordOrder(val copulaSentenceType: CopulaSentenceType, val copulaType: CopulaType) {
    override fun toString() = "$copulaSentenceType, $copulaType"
}


enum class BasicSovOrder(
    override val references: List<GenericSSO<SyntaxRelations>>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    SOV(listOf(listOf(Subject, Object, Verb).toSampleSpaceObject(1.0)), 565.0),
    SVO(listOf(listOf(Subject, Verb, Object).toSampleSpaceObject(1.0)), 488.0),
    VSO(listOf(listOf(Verb, Subject, Object).toSampleSpaceObject(1.0)), 95.0),
    VOS(listOf(listOf(Verb, Object, Subject).toSampleSpaceObject(1.0)), 25.0),
    OVS(listOf(listOf(Object, Verb, Subject).toSampleSpaceObject(1.0)), 11.0),
    OSV(listOf(listOf(Object, Subject, Verb).toSampleSpaceObject(1.0)), 4.0),
    Two(listOf(), 67.0),
    None(listOf(SOV, SVO, VSO, VOS, OVS, OSV).flatMap { it.references }, 122.0)
}

enum class NominalGroupOrder(
    override val references: List<GenericSSO<SyntaxRelations>>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    //TODO no data on that
    DN(listOf(listOf(Definition, Subject).toSampleSpaceObject(1.0)), 100.0),
    ND(listOf(listOf(Subject, Definition).toSampleSpaceObject(1.0)), 100.0),
    None(listOf(DN, ND).flatMap { it.references }, 100.0)
}
