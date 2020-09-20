package shmp.language.syntax.clause.realization

import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.arranger.RelationArranger
import kotlin.random.Random


class NominalClause(
    val noun: Word,
    val definitions: List<NounDefinerClause>,
    val additionalCategories: List<CategoryValue> = listOf()
) : SyntaxClause {
    init {
        if (noun.semanticsCore.speechPart != SpeechPart.Noun)
            throw SyntaxException("$noun is not a noun")
    }

    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode {
        val node = noun.wordToNode(
            changeParadigm,
            RelationArranger(changeParadigm.wordOrder.nominalGroupOrder),
            SyntaxRelation.Subject,
            additionalCategories
        )

        definitions
            .map { it.toNode(changeParadigm, random) }
            .forEach {
                node.addStrayChild(SyntaxRelation.Definition, it)
            }

        return node
    }
}
