package shmp.language.syntax.arranger

import shmp.language.syntax.clause.translation.NonJoinedClause
import shmp.language.syntax.WordSequence
import kotlin.random.Random


interface Arranger {
    fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence
}
