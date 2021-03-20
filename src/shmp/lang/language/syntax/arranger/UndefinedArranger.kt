package shmp.lang.language.syntax.arranger

import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.clause.translation.NonJoinedClause
import kotlin.random.Random


object UndefinedArranger : Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        throw SyntaxException("The order is undefined")
    }
}
