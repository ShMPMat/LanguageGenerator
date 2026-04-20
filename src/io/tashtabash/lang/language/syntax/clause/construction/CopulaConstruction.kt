package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.CopulaClause
import io.tashtabash.lang.language.syntax.clause.realization.NominalClause
import io.tashtabash.lang.language.syntax.clause.realization.NullCopulaClause
import io.tashtabash.lang.language.syntax.clause.realization.ParticleCopulaClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbalCopulaClause
import io.tashtabash.lang.language.syntax.context.Context


abstract class CopulaConstruction : Construction {
    abstract fun apply(
        subject: NominalClause,
        complement: NominalClause,
        language: Language,
        context: Context
    ): CopulaClause

    override fun toString(): String = this.javaClass.simpleName + " copula"

    object Verb : CopulaConstruction() {
        override fun apply(
            subject: NominalClause,
            complement: NominalClause,
            language: Language,
            context: Context
        ): VerbalCopulaClause {
            val verb = language.lexis.getFunctionWord(this)

            return VerbalCopulaClause(
                verb,
                language.changeParadigm.syntaxLogic.resolveVerbForm(
                    language,
                    verb.semanticsCore.speechPart,
                    context
                ),
                subject,
                complement
            )
        }
    }

    object Particle : CopulaConstruction() {
        override fun apply(
            subject: NominalClause,
            complement: NominalClause,
            language: Language,
            context: Context
        ) = ParticleCopulaClause(
            language.lexis.getFunctionWord(this),
            subject,
            complement
        )
    }

    object None : CopulaConstruction() {
        override fun apply(
            subject: NominalClause,
            complement: NominalClause,
            language: Language,
            context: Context
        ) = NullCopulaClause(subject, complement)
    }
}
