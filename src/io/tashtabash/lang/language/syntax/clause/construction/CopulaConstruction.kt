package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.CopulaClause
import io.tashtabash.lang.language.syntax.clause.realization.NominalClause
import io.tashtabash.lang.language.syntax.clause.realization.NullCopulaClause
import io.tashtabash.lang.language.syntax.clause.realization.ParticleCopulaClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbalCopulaClause
import io.tashtabash.lang.language.syntax.context.Context


interface CopulaConstruction : Construction {
    fun apply(
        subject: NominalClause,
        complement: NominalClause,
        language: Language,
        context: Context
    ): CopulaClause

    object Verb : CopulaConstruction {
        override fun apply(
            subject: NominalClause,
            complement: NominalClause,
            language: Language,
            context: Context
        ): VerbalCopulaClause {
            val verb = language.lexis.getCopulaWord(this)

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

    object Particle : CopulaConstruction {
        override fun apply(
            subject: NominalClause,
            complement: NominalClause,
            language: Language,
            context: Context
        ) = ParticleCopulaClause(
            language.lexis.getCopulaWord(this),
            subject,
            complement
        )
    }

    object None : CopulaConstruction {
        override fun apply(
            subject: NominalClause,
            complement: NominalClause,
            language: Language,
            context: Context
        ) = NullCopulaClause(subject, complement)
    }
}
