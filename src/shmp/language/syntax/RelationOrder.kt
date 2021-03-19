package shmp.language.syntax

import shmp.language.syntax.SyntaxRelation.*
import shmp.language.syntax.clause.translation.CopulaSentenceType
import shmp.language.syntax.features.CopulaType
import shmp.random.SampleSpaceObject
import kotlin.random.Random


interface RelationOrder {
    val referenceOrder: (Random) -> List<SyntaxRelation>
}


class SubstitutingOrder(
    val relationOrder: RelationOrder,
    val substituteFun: (List<SyntaxRelation>, Random) -> List<SyntaxRelation>
): RelationOrder {
    override val referenceOrder: (Random) -> List<SyntaxRelation>
        get() = { random ->
            val result = relationOrder.referenceOrder(random)

            substituteFun(result, random)
        }
}


class NestedOrder(
    val outerOrder: RelationOrder,
    val innerOrder: RelationOrder,
    val nestedRelation: SyntaxRelation

): RelationOrder {
    override val referenceOrder: (Random) -> List<SyntaxRelation>
        get() = { random ->
            val constructedInnerOrder = innerOrder.referenceOrder(random)
            val constructedOuterOrder = outerOrder.referenceOrder(random)

            constructedOuterOrder.takeWhile { it != nestedRelation } +
                    constructedInnerOrder +
                    constructedOuterOrder.takeLastWhile { it != nestedRelation }
        }

    override fun toString() = "Order by $innerOrder as $nestedRelation and then order by $outerOrder"
}

class StaticOrder(
    val order: List<SyntaxRelation>
): RelationOrder {
    override val referenceOrder: (Random) -> List<SyntaxRelation>
        get() = {
            order
        }

    override fun toString() = "Order of " + order.joinToString(", ")
}

class SovOrder(override val referenceOrder: (Random) -> List<SyntaxRelation>, val name: String) : RelationOrder {
    override fun toString() = name
}

data class CopulaWordOrder(val copulaSentenceType: CopulaSentenceType, val copulaType: CopulaType) {
    override fun toString() = "$copulaSentenceType, $copulaType"
}


enum class BasicSovOrder(
    override val referenceOrder: (Random) -> List<SyntaxRelation>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    SOV({ listOf(Subject, Object, Verb) }, 565.0),
    SVO({ listOf(Subject, Verb, Object) }, 488.0),
    VSO({ listOf(Verb, Subject, Object) }, 95.0),
    VOS({ listOf(Verb, Object, Subject) }, 25.0),
    OVS({ listOf(Object, Verb, Subject) }, 11.0),
    OSV({ listOf(Object, Subject, Verb) }, 4.0),
    Two({ throw shmp.generator.GeneratorException("Proper SOV order wasn't generated") }, 67.0),
    None({ SOV.referenceOrder(it).shuffled(it) }, 122.0)
}

enum class NominalGroupOrder(
    override val referenceOrder: (Random) -> List<SyntaxRelation>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    //TODO no data on that
    DN({ listOf(Definition, Subject) }, 100.0),
    ND({ listOf(Subject, Definition) }, 100.0),
    None({ DN.referenceOrder(it).shuffled(it) }, 100.0)
}
