package shmp.language.syntax.clause

import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.lexis.Word


abstract class NounDefinerClause : SyntaxClause

class AdjectiveClause(val adjective: Word) : NounDefinerClause() {
    init {
        if (adjective.semanticsCore.speechPart != SpeechPart.Adjective)
            throw LanguageException("$adjective is not an adjective")
    }

    override fun toNode(language: Language) = adjective.toNode(
        language,
        listOf()
    )
}
