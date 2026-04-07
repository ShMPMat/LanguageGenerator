package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.*
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class CopulaDescription(val subject: NominalDescription, val complement: NominalDescription) : ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random): CopulaClause {
        val subjectClause = subject.toClause(language, context, random)
        val complementClause = complement.toClause(language, context, random)

        return language.changeParadigm.syntaxParadigm.copula.copula
            .randomUnwrappedElement()
            .apply(subjectClause, complementClause, language, context)
    }
}
