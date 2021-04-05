package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.category.Case
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.arranger.PassingSingletonArranger
import kotlin.random.Random


abstract class NounDefinerClause : SyntaxClause {
    abstract val relationFromNoun: SyntaxRelation
}

class AdjectiveClause(val adjective: Word) : NounDefinerClause() {
    init {
        if (adjective.semanticsCore.speechPart.type != SpeechPart.Adjective)
            throw SyntaxException("$adjective is not an adjective")
    }

    override val relationFromNoun = SyntaxRelation.Definition

    override fun toNode(language: Language, random: Random) =
        adjective.wordToNode(
            PassingSingletonArranger,
            SyntaxRelation.Definition
        )
}

class PossessorClause(val nominalClause: NominalClause) : NounDefinerClause() {
    override val relationFromNoun = SyntaxRelation.Possessor

    override fun toNode(language: Language, random: Random) =
        nominalClause.toNode(language, random).apply {
            typeForChildren = SyntaxRelation.Possessed

            if (word.semanticsCore.speechPart.type == SpeechPart.PersonalPronoun)
                isDropped = true

            language.changeParadigm.wordChangeParadigm.getSpeechPartParadigm(word.semanticsCore.speechPart).categories
                .firstOrNull { it.category is Case }
                ?.actualSourcedValues
                ?.firstOrNull()
                ?.let {
                    categoryValues.add(it.categoryValue)
                }
        }
}
