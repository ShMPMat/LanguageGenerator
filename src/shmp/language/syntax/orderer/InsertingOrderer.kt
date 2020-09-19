package shmp.language.syntax.orderer

import shmp.language.syntax.SyntaxException
import shmp.language.syntax.WordSequence
import shmp.language.syntax.clause.translation.NonJoinedClause


abstract class InsertingOrderer: Orderer {
    override fun orderClauses(clauses: List<NonJoinedClause>): WordSequence {
        if (clauses.size != 2)
            throw SyntaxException("Got clause list of size ${clauses.size}, expected size 2")

        return insert(clauses[0], clauses[1])
    }

    protected abstract fun insert(toInsert: NonJoinedClause, target: NonJoinedClause): WordSequence
}


object InsertLast: InsertingOrderer() {
    override fun insert(toInsert: NonJoinedClause, target: NonJoinedClause) =
        target.second + toInsert.second
}

object InsertFirst: InsertingOrderer() {
    override fun insert(toInsert: NonJoinedClause, target: NonJoinedClause) =
        toInsert.second + target.second
}
