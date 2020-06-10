package shmp.language.syntax

import shmp.language.category.paradigm.ChangeException
import shmp.language.category.paradigm.NonJoinedClause
import shmp.random.SampleSpaceObject

data class WordOrder(val sovOrder: SovOrder) {
    override fun toString() = sovOrder.toString()
    fun uniteToClause(
        currentNonJoinedClause: NonJoinedClause,
        childrenClauses: MutableList<NonJoinedClause>
    ): Clause {
        return joinSovClauses(childrenClauses + listOf(currentNonJoinedClause))
    }

    private fun joinSovClauses(clauses: List<NonJoinedClause>): Clause {
        val resultWords = clauses
            .sortedBy { (r) ->
                val i = sovOrder.referenceOrder.indexOf(r)
                if (i == -1)
                    throw ChangeException("No Relation $r in a relation order ${sovOrder.referenceOrder}")
                i
            }.flatMap { it.second.words }
        return Clause(resultWords)
    }
}

enum class SovOrder(val referenceOrder: List<SyntaxRelation>, override val probability: Double) : SampleSpaceObject {
    SOV(listOf(SyntaxRelation.Subject, SyntaxRelation.Object, SyntaxRelation.Verb), 565.0),
    SVO(listOf(SyntaxRelation.Subject, SyntaxRelation.Verb, SyntaxRelation.Object), 488.0),
    VSO(listOf(SyntaxRelation.Verb, SyntaxRelation.Subject, SyntaxRelation.Object), 95.0),
    VOS(listOf(SyntaxRelation.Verb, SyntaxRelation.Object, SyntaxRelation.Subject), 25.0),
    OVS(listOf(SyntaxRelation.Object, SyntaxRelation.Verb, SyntaxRelation.Subject), 11.0),
    OSV(listOf(SyntaxRelation.Object, SyntaxRelation.Subject, SyntaxRelation.Verb),4.0),
    None(listOf(SyntaxRelation.Object, SyntaxRelation.Verb, SyntaxRelation.Subject),189.0) //TODO none is none!
}