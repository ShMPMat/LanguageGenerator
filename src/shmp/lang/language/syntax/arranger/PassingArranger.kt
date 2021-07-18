package shmp.lang.language.syntax.arranger

import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.clause.translation.NonJoinedClause
import kotlin.random.Random


object PassingArranger: Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random) =
        WordSequence(clauses.flatMap { it.second.words })
}


object PassingSingletonArranger: Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        if (clauses.size != 1)
            throw SyntaxException("PassingSingletonOrderer got a non-singleton list of size ${clauses.size}")

        return clauses[0].second
    }
}
