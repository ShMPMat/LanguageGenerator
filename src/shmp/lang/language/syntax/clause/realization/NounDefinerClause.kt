package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.arranger.PassingSingletonArranger
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
