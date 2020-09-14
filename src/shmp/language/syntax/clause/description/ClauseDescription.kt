package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.syntax.WordSequence
import shmp.language.syntax.clause.realization.SyntaxClause
import kotlin.random.Random


interface ClauseDescription {
    fun toClause(language: Language, random: Random): SyntaxClause
}


interface UnfoldableClauseDescription: ClauseDescription {
    fun unfold(language: Language, random: Random): WordSequence
}
