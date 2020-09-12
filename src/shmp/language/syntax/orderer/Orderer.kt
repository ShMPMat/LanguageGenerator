package shmp.language.syntax.orderer

import shmp.language.syntax.NonJoinedClause
import shmp.language.syntax.WordSequence
import kotlin.random.Random


interface Orderer {
    fun orderClauses(clauses: List<NonJoinedClause>): WordSequence
}

abstract class RandomizedOrderer(val random: Random): Orderer
