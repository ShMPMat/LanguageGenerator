package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.category.CaseValue
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.arranger.PassingSingletonArranger
import kotlin.random.Random


abstract class NounDefinerClause(val relationFromNoun: SyntaxRelation) : SyntaxClause


class AdjectiveClause(val adjective: Word) : NounDefinerClause(SyntaxRelation.Definition) {
    init {
        if (adjective.semanticsCore.speechPart.type != SpeechPart.Adjective)
            throw SyntaxException("$adjective is not an adjective")
    }

    override fun toNode(language: Language, random: Random) =
        adjective.wordToNode(
            PassingSingletonArranger,
            SyntaxRelation.Definition
        )
}


class PossessorClause(val nominalClause: NominalClause) : NounDefinerClause(SyntaxRelation.Possessor) {
    override fun toNode(language: Language, random: Random) =
        nominalClause.toNode(language, random).apply {
            typeForChildren = SyntaxRelation.Possessor

            if (word.semanticsCore.speechPart.type == SpeechPart.PersonalPronoun)
                isDropped = true

            categoryValues.removeIf { it is CaseValue }
            val newCaseMarkers = language.changeParadigm.syntaxLogic.resolveNonCoreCase(
                CaseValue.Genitive,
                word.semanticsCore.speechPart
            )
            categoryValues.addAll(newCaseMarkers)
        }
}
