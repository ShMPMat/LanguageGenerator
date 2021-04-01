package shmp.lang.language.syntax.clause.description

import shmp.lang.language.Language
import shmp.lang.language.syntax.clause.realization.*
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.features.CopulaType
import shmp.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class CopulaDescription(
    val subject: NominalDescription,
    val complement: NominalDescription
) : ClauseDescription {
    override fun toClause(language: Language, context: Context, random: Random): CopulaClause {
        val subjectClause = subject.toClause(language, context, random)
        val complementClause = complement.toClause(language, context, random)

        return language.changeParadigm.syntaxParadigm.copulaPresence.copulaType
            .randomUnwrappedElement()
            .let { copulaType ->
                when (copulaType) {
                    CopulaType.Verb -> VerbalCopulaClause(
                        language.lexis.getCopulaWord(CopulaType.Verb).let {
                            it.copyWithValues(
                                language.changeParadigm.syntaxLogic.resolveVerbForm(
                                    language,
                                    it.semanticsCore.speechPart,
                                    context
                                )
                            )
                        },
                        subjectClause,
                        complementClause
                    )
                    CopulaType.Particle -> ParticleCopulaClause(
                        language.lexis.getCopulaWord(CopulaType.Particle),
                        subjectClause,
                        complementClause
                    )
                    CopulaType.None -> NullCopulaClause(
                        subjectClause,
                        complementClause
                    )
                }
            }
    }
}
