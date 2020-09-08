package shmp.language.syntax

import shmp.language.category.paradigm.ChangeException
import shmp.language.category.paradigm.NonJoinedClause
import shmp.random.SampleSpaceObject
import kotlin.random.Random


data class WordOrder(private val sovOrder: Map<SentenceType, SovOrder>, val nominalGroupOrder: NominalGroupOrder) {
    fun uniteToClause(
        currentNonJoinedClause: NonJoinedClause,
        childrenClauses: MutableList<NonJoinedClause>,
        sentenceType: SentenceType,
        random: Random
    ): WordSequence {
        if (childrenClauses.isEmpty())
            return currentNonJoinedClause.second

        val fullClauses = childrenClauses + listOf(currentNonJoinedClause)

        return when (currentNonJoinedClause.first) {
            SyntaxRelation.Verb -> orderWithRelation(fullClauses, sovOrder.getValue(sentenceType), random)
            SyntaxRelation.Object -> orderWithRelation(fullClauses, nominalGroupOrder, random)
            SyntaxRelation.Subject -> orderWithRelation(fullClauses, nominalGroupOrder, random)
            else -> throw ChangeException("No ordering for a ${currentNonJoinedClause.first}")
        }

    }

    private fun orderWithRelation(clauses: List<NonJoinedClause>, relationOrder: RelationOrder, random: Random): WordSequence {
        val relation = relationOrder.referenceOrder(random)
        val resultWords = clauses
            .sortedBy { (r) ->
                val i = relation.indexOf(r)
                if (i == -1)
                    throw ChangeException("No Relation $r in a relation order ${relationOrder.referenceOrder}")
                i
            }.flatMap { it.second.words }
        return WordSequence(resultWords)
    }

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
