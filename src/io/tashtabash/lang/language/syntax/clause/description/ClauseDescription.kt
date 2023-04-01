package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.clause.realization.SyntaxClause
import io.tashtabash.lang.language.syntax.context.Context
import kotlin.random.Random


interface ClauseDescription {
    fun toClause(language: Language, context: Context, random: Random): SyntaxClause
}


interface UnfoldableClauseDescription: ClauseDescription {
    fun unfold(language: Language, context: Context, random: Random): WordSequence
}
