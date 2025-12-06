package io.tashtabash.lang.language.syntax.arranger

import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.NonJoinedClause
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import kotlin.random.Random


interface Arranger {
    fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random): List<E>

    fun orderClauses(clauses: List<NonJoinedClause>, random: Random) = order(clauses, random)
        .foldRight(WordSequence(), WordSequence::plus)
}
