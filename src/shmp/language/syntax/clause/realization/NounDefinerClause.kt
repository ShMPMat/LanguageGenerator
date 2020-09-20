package shmp.language.syntax.clause.realization

import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.arranger.PassingSingletonArranger
import kotlin.random.Random


abstract class NounDefinerClause : SyntaxClause

class AdjectiveClause(val adjective: Word) : NounDefinerClause() {
    init {
        if (adjective.semanticsCore.speechPart != SpeechPart.Adjective)
            throw SyntaxException("$adjective is not an adjective")
    }

    override fun toNode(changeParadigm: ChangeParadigm, random: Random) =
        adjective.wordToNode(
            changeParadigm,
            PassingSingletonArranger,
            SyntaxRelation.Definition
        )
}
