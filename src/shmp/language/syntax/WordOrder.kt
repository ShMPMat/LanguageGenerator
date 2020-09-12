package shmp.language.syntax

import shmp.random.SampleSpaceObject
import kotlin.random.Random


data class WordOrder(val sovOrder: Map<SentenceType, SovOrder>, val nominalGroupOrder: NominalGroupOrder) {
    override fun toString() = "$sovOrder, $nominalGroupOrder"
}


interface RelationOrder {
    val referenceOrder: (Random) -> List<SyntaxRelation>
}


class SovOrder(override val referenceOrder: (Random) -> List<SyntaxRelation>, val name: String) : RelationOrder {
    override fun toString() = name
}


enum class BasicSovOrder(
    override val referenceOrder: (Random) -> List<SyntaxRelation>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    SOV({ listOf(SyntaxRelation.Subject, SyntaxRelation.Object, SyntaxRelation.Verb) }, 565.0),
    SVO({ listOf(SyntaxRelation.Subject, SyntaxRelation.Verb, SyntaxRelation.Object) }, 488.0),
    VSO({ listOf(SyntaxRelation.Verb, SyntaxRelation.Subject, SyntaxRelation.Object) }, 95.0),
    VOS({ listOf(SyntaxRelation.Verb, SyntaxRelation.Object, SyntaxRelation.Subject) }, 25.0),
    OVS({ listOf(SyntaxRelation.Object, SyntaxRelation.Verb, SyntaxRelation.Subject) }, 11.0),
    OSV({ listOf(SyntaxRelation.Object, SyntaxRelation.Subject, SyntaxRelation.Verb) }, 4.0),
    Two({ throw shmp.generator.GeneratorException("Proper SOV order wasn't generated") }, 67.0),
    None({ SOV.referenceOrder(it).shuffled(it) }, 122.0)
}

enum class NominalGroupOrder(
    override val referenceOrder: (Random) -> List<SyntaxRelation>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    //TODO no data on that
    DN({ listOf(SyntaxRelation.Definition, SyntaxRelation.Subject, SyntaxRelation.Object) }, 100.0),
    ND({ listOf(SyntaxRelation.Subject, SyntaxRelation.Object, SyntaxRelation.Definition) }, 100.0),
    None({ DN.referenceOrder(it).shuffled(it) }, 100.0)
}
