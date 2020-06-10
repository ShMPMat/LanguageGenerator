package shmp.language.syntax

import shmp.language.category.paradigm.ChangeException
import shmp.language.category.paradigm.NonJoinedClause
import shmp.random.SampleSpaceObject
import kotlin.random.Random

data class WordOrder(val sovOrder: SovOrder, val nominalGroupOrder: NominalGroupOrder) {
    fun uniteToClause(
        currentNonJoinedClause: NonJoinedClause,
        childrenClauses: MutableList<NonJoinedClause>,
        random: Random
    ): Clause {
        if (childrenClauses.isEmpty()) return currentNonJoinedClause.second

        val fullClauses = childrenClauses + listOf(currentNonJoinedClause)
        return when (currentNonJoinedClause.first) {
            SyntaxRelation.Verb -> orderWithRelation(fullClauses, sovOrder, random)
            SyntaxRelation.Object -> orderWithRelation(fullClauses, nominalGroupOrder, random)
            SyntaxRelation.Subject -> orderWithRelation(fullClauses, nominalGroupOrder, random)
            else -> throw ChangeException("No ordering for a ${currentNonJoinedClause.first}")
        }

    }

    private fun orderWithRelation(clauses: List<NonJoinedClause>, relationOrder: RelationOrder, random: Random): Clause {
        val resultWords = clauses
            .sortedBy { (r) ->
                val i = relationOrder.referenceOrder(random).indexOf(r)
                if (i == -1)
                    throw ChangeException("No Relation $r in a relation order ${sovOrder.referenceOrder}")
                i
            }.flatMap { it.second.words }
        return Clause(resultWords)
    }

    override fun toString() = "$sovOrder, $nominalGroupOrder"
}

interface RelationOrder {
    val referenceOrder: (Random) -> List<SyntaxRelation>
}

enum class SovOrder(
    override val referenceOrder: (Random) -> List<SyntaxRelation>,
    override val probability: Double
) : SampleSpaceObject, RelationOrder {
    SOV({ listOf(SyntaxRelation.Subject, SyntaxRelation.Object, SyntaxRelation.Verb) }, 565.0),
    SVO({ listOf(SyntaxRelation.Subject, SyntaxRelation.Verb, SyntaxRelation.Object) }, 488.0),
    VSO({ listOf(SyntaxRelation.Verb, SyntaxRelation.Subject, SyntaxRelation.Object) }, 95.0),
    VOS({ listOf(SyntaxRelation.Verb, SyntaxRelation.Object, SyntaxRelation.Subject) }, 25.0),
    OVS({ listOf(SyntaxRelation.Object, SyntaxRelation.Verb, SyntaxRelation.Subject) }, 11.0),
    OSV({ listOf(SyntaxRelation.Object, SyntaxRelation.Subject, SyntaxRelation.Verb) }, 4.0),
    None({ SOV.referenceOrder(it).shuffled(it) }, 189.0)
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