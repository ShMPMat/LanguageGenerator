package shmp.lang.language.syntax.arranger

import shmp.lang.language.syntax.clause.translation.NonJoinedClause
import shmp.lang.language.syntax.WordSequence
import kotlin.random.Random


interface Arranger {
    fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence
}
