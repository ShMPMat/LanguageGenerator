package shmp.language.syntax.clause.description

import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.syntax.clause.realization.NullCopulaClause
import shmp.language.syntax.clause.realization.ParticleCopulaClause
import shmp.language.syntax.clause.realization.VerbalCopulaClause
import shmp.language.syntax.features.CopulaType
import shmp.random.randomUnwrappedElement
import kotlin.random.Random


class CopulaDescription(
    val subject: NominalDescription,
    val complement: NominalDescription
): ClauseDescription {
    override fun toClause(language: Language, random: Random) =
        language.changeParadigm.syntaxParadigm.copulaPresence.copulaType.let { copulaType ->
            when (randomUnwrappedElement(copulaType, random)) {
                CopulaType.Verb -> VerbalCopulaClause(
                    language.lexis.copula ?: throw LanguageException("No copula in Language with verbal copula"),
                    subject.toClause(language, random),
                    complement.toClause(language, random)
                )
                CopulaType.Particle -> ParticleCopulaClause(
                    language.lexis.copula ?: throw LanguageException("No copula in Language with particle copula"),
                    subject.toClause(language, random),
                    complement.toClause(language, random)
                )
                CopulaType.None -> NullCopulaClause(
                    subject.toClause(language, random),
                    complement.toClause(language, random)
                )
            }
        }
}
