package shmp.language.syntax.orderer

import shmp.language.LanguageException
import shmp.language.syntax.NonJoinedClause
import shmp.language.syntax.WordSequence



object PassingOrderer: Orderer {
    override fun orderClauses(clauses: List<NonJoinedClause>): WordSequence {
        return WordSequence(clauses.flatMap { it.second.words })
    }
}


object PassingSingletonOrderer: Orderer {
    override fun orderClauses(clauses: List<NonJoinedClause>): WordSequence {
        if (clauses.size != 1)
            throw LanguageException("PassingSingletonOrderer got a non-singleton list of size ${clauses.size}")

        return clauses[0].second
    }
}
