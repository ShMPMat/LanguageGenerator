package shmp.lang.language.syntax.arranger

import shmp.lang.language.category.paradigm.ChangeException
import shmp.lang.language.syntax.clause.translation.NonJoinedClause
import shmp.lang.language.syntax.RelationOrder
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence
import kotlin.random.Random


class RelationArranger(val relationOrder: RelationOrder) : Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        val relation = relationOrder.chooseReferenceOrder

        return order(clauses, relation)
    }

    override fun toString() = "In order of $relationOrder"
}

//Relations are listed from most to least close
class RelationsArranger(val relationOrders: List<Pair<RelationOrder, SyntaxRelation>>) : Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        val relations = relationOrders
            .map { (o, r) -> o.chooseReferenceOrder to r }

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