package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.clause.realization.SyntaxClause
import kotlin.random.Random


interface ClauseDescription {
    fun toClause(language: Language, random: Random): SyntaxClause
}


interface UnfoldableClauseDescription: ClauseDescription {
    fun unfold(language: Language, random: Random): WordSequence
}
