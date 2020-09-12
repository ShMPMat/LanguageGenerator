package shmp.language.syntax.clause

import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.category.paradigm.SentenceChangeParadigm
import shmp.language.lexis.Word
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.orderer.PassingSingletonOrderer
import kotlin.random.Random


abstract class NounDefinerClause : SyntaxClause

class AdjectiveClause(val adjective: Word) : NounDefinerClause() {
    init {
        if (adjective.semanticsCore.speechPart != SpeechPart.Adjective)
            throw LanguageException("$adjective is not an adjective")
    }

    override fun toNode(sentenceChangeParadigm: SentenceChangeParadigm, random: Random) =
        adjective.wordToNode(
            sentenceChangeParadigm,
            PassingSingletonOrderer
        )
}
