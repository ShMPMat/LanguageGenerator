package shmp.language.syntax.clause

import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxRelation


class NominalClause(
    val noun: Word,
    val definitions: List<NounDefinerClause>,
    val additionalCategories: List<CategoryValue> = listOf()
) : SyntaxClause {
    init {
        if (noun.semanticsCore.speechPart != SpeechPart.Noun)
            throw LanguageException("$noun is not a noun")
    }

    override fun toNode(language: Language): SentenceNode {
        val node = noun.toNode(language, additionalCategories)

        definitions
            .map { it.toNode(language) }
            .forEach {
                it.setRelation(SyntaxRelation.Subject, node, false)
                node.addChild(SyntaxRelation.Definition, it)
            }

        return node
    }
}
