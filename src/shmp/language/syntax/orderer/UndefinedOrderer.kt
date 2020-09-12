package shmp.language.syntax.orderer

import shmp.language.LanguageException
import shmp.language.syntax.clause.translation.NonJoinedClause
import shmp.language.syntax.WordSequence


object UndefinedOrderer : Orderer {
    override fun orderClauses(clauses: List<NonJoinedClause>): WordSequence {
        throw LanguageException("The order is undefined")
    }
}
