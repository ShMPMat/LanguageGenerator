package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.PassingSingletonArranger
import kotlin.random.Random


abstract class NounDefinerClause(val relationFromNoun: SyntaxRelation) : SyntaxClause


class AdjectiveClause(val adjective: Word) : NounDefinerClause(SyntaxRelation.Definition) {
    init {
        if (adjective.semanticsCore.speechPart.type != SpeechPart.Adjective)
            throw SyntaxException("$adjective is not an adjective")
    }

    override fun toNode(language: Language, random: Random) =
        adjective.wordToNode(
            SyntaxRelation.Definition,
            adjective.categoryValues.map { it.categoryValue },
            PassingSingletonArranger
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
            categoryValues += newCaseMarkers
        }
}
