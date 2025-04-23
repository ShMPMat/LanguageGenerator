package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.SyntaxClause
import io.tashtabash.lang.language.syntax.clause.realization.UnfoldableClause
import io.tashtabash.lang.language.syntax.context.Context
import kotlin.random.Random


/**
 * The basic building block of the meta-language for describing meaning, which can be
 * transformed into a SyntaxClause.
 */
interface ClauseDescription {
    fun toClause(language: Language, context: Context, random: Random): SyntaxClause
}


interface UnfoldableClauseDescription: ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random): UnfoldableClause
}
