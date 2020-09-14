package shmp.language.syntax.clause.realization

import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.syntax.SyntaxParadigm
import shmp.language.lexis.Word
import shmp.language.syntax.orderer.PassingSingletonOrderer
import kotlin.random.Random


abstract class NounDefinerClause : SyntaxClause

class AdjectiveClause(val adjective: Word) : NounDefinerClause() {
    init {
        if (adjective.semanticsCore.speechPart != SpeechPart.Adjective)
            throw LanguageException("$adjective is not an adjective")
    }

    override fun toNode(syntaxParadigm: SyntaxParadigm, random: Random) =
        adjective.wordToNode(
            syntaxParadigm,
            PassingSingletonOrderer
        )
}
