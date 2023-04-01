package io.tashtabash.lang.language.syntax.arranger

import io.tashtabash.lang.language.category.paradigm.ChangeException
import io.tashtabash.lang.language.syntax.RelationOrder
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import kotlin.random.Random


class RelationArranger(val relationOrder: RelationOrder) : Arranger {
    override fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random): List<E> {
        val relation = relationOrder.chooseReferenceOrder

        return order(pairs, relation)
    }

    override fun toString() = "In order of $relationOrder"
}

//Relations are listed from most to least close
class RelationsArranger(val relationOrders: List<Pair<RelationOrder, SyntaxRelation>>) : Arranger {
    override fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random): List<E> {
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

        return order(pairs, relation)
    }
}

internal fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, relation: List<SyntaxRelation>) = pairs
    .sortedBy { (r) ->
        relation.indexOf(r)
            .takeIf { it != -1 }
            ?: throw ChangeException("No Relation $r in a relation order $relation")
    }
    .map { it.second }
