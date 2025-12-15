package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.clause.realization.*
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class CopulaDescription(val subject: NominalDescription, val complement: NominalDescription) : ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random): CopulaClause {
        val subjectClause = subject.toClause(language, context, random)
        val complementClause = complement.toClause(language, context, random)

        return language.changeParadigm.syntaxParadigm.copulaPresence.copulaType
            .randomUnwrappedElement()
            .let { copulaType ->
                when (copulaType) {
                    CopulaType.Verb -> {
                        val verb = language.lexis.getCopulaWord(CopulaType.Verb)

                        VerbalCopulaClause(
                            verb,
                            language.changeParadigm.syntaxLogic.resolveVerbForm(
                                language,
                                verb.semanticsCore.speechPart,
                                context
                            ),
                            subjectClause,
                            complementClause
                        )
                    }
                    CopulaType.Particle -> ParticleCopulaClause(
                        language.lexis.getCopulaWord(CopulaType.Particle),
                        subjectClause,
                        complementClause
                    )
                    CopulaType.None -> NullCopulaClause(subjectClause, complementClause)
                }
            }
    }
}
