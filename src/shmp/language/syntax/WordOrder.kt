package shmp.language.syntax

import shmp.language.syntax.SyntaxRelation.*
import shmp.language.syntax.clause.translation.SentenceType
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
    SOV({ listOf(Subject, Object, SubjectCompliment, Verb) }, 565.0),
    SVO({ listOf(Subject, Verb, Object, SubjectCompliment) }, 488.0),
    VSO({ listOf(Verb, Subject, Object, SubjectCompliment) }, 95.0),
    VOS({ listOf(Verb, Object, SubjectCompliment, Subject) }, 25.0),
    OVS({ listOf(Object, SubjectCompliment, Verb, Subject) }, 11.0),
    OSV({ listOf(Object, SubjectCompliment, Subject, Verb) }, 4.0),
    Two({ throw shmp.generator.GeneratorException("Proper SOV order wasn't generated") }, 67.0),
    None({ SOV.referenceOrder(it).shuffled(it) }, 122.0)
}

enum class NominalGroupOrder(
    override val referenceOrder: (Random) -> List<SyntaxRelation>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    //TODO no data on that
    DN({ listOf(Definition, Subject, Object, SubjectCompliment) }, 100.0),
    ND({ listOf(Subject, Object, SubjectCompliment, Definition) }, 100.0),
    None({ DN.referenceOrder(it).shuffled(it) }, 100.0)
}
