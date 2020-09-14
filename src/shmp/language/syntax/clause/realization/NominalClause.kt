package shmp.language.syntax.clause.realization

import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.syntax.SyntaxParadigm
import shmp.language.lexis.Word
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.orderer.RelationOrderer
import kotlin.random.Random


class NominalClause(
    val noun: Word,
    val definitions: List<NounDefinerClause>,
    val additionalCategories: List<CategoryValue> = listOf()
) : SyntaxClause {
    init {
        if (noun.semanticsCore.speechPart != SpeechPart.Noun)
            throw LanguageException("$noun is not a noun")
    }

    override fun toNode(syntaxParadigm: SyntaxParadigm, random: Random): SentenceNode {
        val node = noun.wordToNode(
            syntaxParadigm,
            RelationOrderer(syntaxParadigm.wordOrder.nominalGroupOrder, random),
            additionalCategories
        )

        definitions
            .map { it.toNode(syntaxParadigm, random) }
            .forEach {
                it.setRelation(SyntaxRelation.Subject, node, false)
                node.addChild(SyntaxRelation.Definition, it)
            }

        return node
    }
}
