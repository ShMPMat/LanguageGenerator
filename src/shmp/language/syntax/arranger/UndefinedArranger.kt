package shmp.language.syntax.arranger

import shmp.language.syntax.SyntaxException
import shmp.language.syntax.WordSequence
import shmp.language.syntax.clause.translation.NonJoinedClause
import kotlin.random.Random


object UndefinedArranger : Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        throw SyntaxException("The order is undefined")
    }
}
