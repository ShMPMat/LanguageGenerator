package shmp.language.syntax.arranger

import shmp.language.category.paradigm.ChangeException
import shmp.language.syntax.clause.translation.NonJoinedClause
import shmp.language.syntax.RelationOrder
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.WordSequence
import kotlin.random.Random


class RelationArranger(val relationOrder: RelationOrder) : Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        val relation = relationOrder.referenceOrder(random)

        return order(clauses, relation)
    }
}

//Relations are listed from most to least close
class RelationsArranger(val relationOrders: List<Pair<RelationOrder, SyntaxRelation>>) : Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        val relations = relationOrders
            .map { (o, r) -> o.referenceOrder(random) to r }

        var relation = relations[0].first

        for ((nextOrder, nextRelation) in relations.drop(1)) {
            if (relation.count { it == nextRelation } != 1)
                throw SyntaxException("RelationsOrderer cannot insert Orders")

            relation = nextOrder.takeWhile { it != nextRelation } +
                    relation +
                    nextOrder.takeLastWhile { it != nextRelation }
        }

        return order(clauses, relation)
    }
}

internal fun order(clauses: List<NonJoinedClause>, relation: List<SyntaxRelation>): WordSequence {
    val resultWords = clauses
        .sortedBy { (r) ->
            relation.indexOf(r)
                .takeIf { it != -1 }
                ?: throw ChangeException("No Relation $r in a relation order $relation")
        }
        .flatMap { it.second.words }

    return WordSequence(resultWords)
}