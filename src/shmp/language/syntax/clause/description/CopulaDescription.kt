package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.syntax.clause.realization.CopulaClause
import shmp.language.syntax.clause.realization.NullCopulaClause
import shmp.language.syntax.clause.realization.ParticleCopulaClause
import shmp.language.syntax.clause.realization.VerbalCopulaClause
import shmp.language.syntax.features.CopulaType
import shmp.random.randomUnwrappedElement
import shmp.random.singleton.randomUnwrappedElement
import kotlin.random.Random


class CopulaDescription(
    val subject: NominalDescription,
    val complement: NominalDescription
) : ClauseDescription {
    override fun toClause(language: Language, random: Random): CopulaClause {
        val subjectClause = subject.toClause(language, random)
        val complementClause = complement.toClause(language, random)

        return language.changeParadigm.syntaxParadigm.copulaPresence.copulaType
            .randomUnwrappedElement()
            .let { copulaType ->
                when (copulaType) {
                    CopulaType.Verb -> VerbalCopulaClause(
                        language.lexis.getCopulaWord(CopulaType.Verb),
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
