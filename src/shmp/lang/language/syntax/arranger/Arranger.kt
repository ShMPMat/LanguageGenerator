package shmp.lang.language.syntax.arranger

import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.translation.NonJoinedClause
import shmp.lang.language.syntax.WordSequence
import kotlin.random.Random


interface Arranger {
    fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random): List<E>

    fun orderClauses(clauses: List<NonJoinedClause>, random: Random) = order(clauses, random)
        .foldRight(WordSequence(listOf()), WordSequence::plus)
}
