package io.tashtabash.lang.language.syntax.arranger

import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import kotlin.random.Random


object PassingArranger: Arranger {
    override fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random) = pairs.map { it.second }
}


object PassingSingletonArranger: Arranger {
    override fun <E> order(pairs: List<Pair<SyntaxRelation, E>>, random: Random): List<E> {
        if (pairs.size != 1)
            throw SyntaxException("PassingSingletonOrderer got a non-singleton list of size ${pairs.size}")

        return listOf(pairs[0].second)
    }
}
