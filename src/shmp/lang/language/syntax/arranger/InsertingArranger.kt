package shmp.lang.language.syntax.arranger

import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.clause.translation.NonJoinedClause
import kotlin.random.Random


abstract class InsertingArranger: Arranger {
    override fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random): List<E> {
        if (pairs.size != 2)
            throw SyntaxException("Got clause list of size ${pairs.size}, expected size 2")

        return insert(pairs[0], pairs[1])
    }

    protected abstract fun <E> insert(toInsert: Pair<SyntaxRelation, E>, target: Pair<SyntaxRelation, E>): List<E>
}


object InsertLast: InsertingArranger() {
    override fun <E> insert(toInsert: Pair<SyntaxRelation, E>, target: Pair<SyntaxRelation, E>) =
        listOf(target.second, toInsert.second)

}

object InsertFirst: InsertingArranger() {
    override fun <E> insert(toInsert: Pair<SyntaxRelation, E>, target: Pair<SyntaxRelation, E>) =
        listOf(toInsert.second, target.second)
}
