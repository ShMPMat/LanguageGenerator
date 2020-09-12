package shmp.language.syntax.orderer

import shmp.language.category.paradigm.ChangeException
import shmp.language.syntax.NonJoinedClause
import shmp.language.syntax.RelationOrder
import shmp.language.syntax.WordSequence
import kotlin.random.Random


class RelationOrderer(val relationOrder: RelationOrder, random: Random) : RandomizedOrderer(random) {
    override fun orderClauses(clauses: List<NonJoinedClause>): WordSequence {
        val relation = relationOrder.referenceOrder(random)
        val resultWords = clauses
            .sortedBy { (r) ->
                val i = relation.indexOf(r)
                if (i == -1)
                    throw ChangeException("No Relation $r in a relation order ${relationOrder.referenceOrder}")
                i
            }
            .flatMap { it.second.words }

        return WordSequence(resultWords)
    }
}
