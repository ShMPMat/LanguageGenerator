package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.PassingSingletonArranger
import io.tashtabash.lang.language.syntax.transformer.AddCategoryTransformer
import kotlin.random.Random


abstract class NounDefinerClause(val relationFromNoun: SyntaxRelation) : SyntaxClause


class AdjectiveClause(
    val adjective: Word,
    val categories: SourcedCategoryValues = listOf()
) : NounDefinerClause(SyntaxRelation.Definition) {
    init {
        if (adjective.semanticsCore.speechPart.type != SpeechPart.Adjective)
            throw SyntaxException("$adjective is not an adjective")
    }

    override fun toNode(language: Language, random: Random) =
        adjective.toNode(
            SyntaxRelation.Definition,
            categories.map { it.categoryValue },
            PassingSingletonArranger
        )
}


class PossessorClause(val nominalClause: NominalClause) : NounDefinerClause(SyntaxRelation.Possessor) {
    override fun toNode(language: Language, random: Random) =
        nominalClause.toNode(language, random).apply {
            typeForChildren = SyntaxRelation.Possessor
            AddCategoryTransformer(SyntaxRelation.Possessor).apply(this, language.changeParadigm.syntaxLogic)
        }
}
