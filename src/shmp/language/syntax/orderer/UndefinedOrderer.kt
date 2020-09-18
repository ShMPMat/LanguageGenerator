package shmp.language.syntax.orderer

import shmp.language.syntax.SyntaxException
import shmp.language.syntax.WordSequence
import shmp.language.syntax.clause.translation.NonJoinedClause


object UndefinedOrderer : Orderer {
    override fun orderClauses(clauses: List<NonJoinedClause>): WordSequence {
        throw SyntaxException("The order is undefined")
    }
}
