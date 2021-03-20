package shmp.lang.language.syntax.arranger

import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.clause.translation.NonJoinedClause
import kotlin.random.Random


abstract class InsertingArranger: Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        if (clauses.size != 2)
            throw SyntaxException("Got clause list of size ${clauses.size}, expected size 2")

        return insert(clauses[0], clauses[1])
    }

    protected abstract fun insert(toInsert: NonJoinedClause, target: NonJoinedClause): WordSequence
}


object InsertLast: InsertingArranger() {
    override fun insert(toInsert: NonJoinedClause, target: NonJoinedClause) =
        target.second + toInsert.second
}

object InsertFirst: InsertingArranger() {
    override fun insert(toInsert: NonJoinedClause, target: NonJoinedClause) =
        toInsert.second + target.second
}
