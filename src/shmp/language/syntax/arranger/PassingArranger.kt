package shmp.language.syntax.arranger

import shmp.language.syntax.SyntaxException
import shmp.language.syntax.WordSequence
import shmp.language.syntax.clause.translation.NonJoinedClause
import kotlin.random.Random


object PassingArranger: Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        return WordSequence(clauses.flatMap { it.second.words })
    }
}


object PassingSingletonArranger: Arranger {
    override fun orderClauses(clauses: List<NonJoinedClause>, random: Random): WordSequence {
        if (clauses.size != 1)
            throw SyntaxException("PassingSingletonOrderer got a non-singleton list of size ${clauses.size}")

        return clauses[0].second
    }
}
