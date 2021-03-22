package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.CategoryValue
import shmp.lang.language.Language
import shmp.lang.language.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.syntax.SyntaxException
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.lang.language.syntax.features.WordSyntaxRole
import kotlin.random.Random


class NominalClause(
    val nominal: Word,
    val definitions: List<NounDefinerClause>,
    val additionalCategories: List<CategoryValue> = listOf()
) : SyntaxClause {
    init {
        if (nominal.semanticsCore.speechPart !in listOf(SpeechPart.Noun, SpeechPart.Pronoun))
            throw SyntaxException("$nominal is not a noun or pronoun")
    }

    override fun toNode(language: Language, random: Random): SentenceNode {
        val changeParadigm = language.changeParadigm

        val node = nominal
            .let {
                if (nominal.semanticsCore.speechPart == SpeechPart.Pronoun)
                    it.copy(syntaxRole = WordSyntaxRole.PersonalPronoun)
                else it
            }
            .wordToNode(
                changeParadigm,
                RelationArranger(changeParadigm.wordOrder.nominalGroupOrder),
                SyntaxRelation.Subject,
                additionalCategories
            )

        definitions
            .map { it.toNode(language, random) }
            .forEach {
                node.addStrayChild(SyntaxRelation.Definition, it)
            }

        return node
    }
}
