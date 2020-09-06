package shmp.language.syntax

import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.category.CategorySource
import shmp.language.lexis.Word


interface SyntaxClause {
    fun toNode(language: Language): SentenceNode
}


abstract class NounDefinerClause : SyntaxClause

class AdjectiveClause(val adjective: Word) : NounDefinerClause() {
    init {
        if (adjective.semanticsCore.speechPart != SpeechPart.Adjective)
            throw LanguageException("$adjective is not an adjective")
    }

    override fun toNode(language: Language) = adjective.toNode(
        language,
        listOf()
    )
}


class NominalClause(
    val noun: Word,
    val definitions: List<NounDefinerClause>,
    val additionalCategories: List<CategoryValue> = listOf()
) : SyntaxClause {
    init {
        if (noun.semanticsCore.speechPart != SpeechPart.Noun)
            throw LanguageException("$noun is not a noun")
    }

    //TODO multiple definitions
    override fun toNode(language: Language): SentenceNode {
        val node = noun.toNode(language, additionalCategories)
        val definers = definitions
            .map { it.toNode(language) }
            .forEach {
                it.setRelation(SyntaxRelation.Subject, node)
                node.setRelation(SyntaxRelation.Definition, it)
            }

        return node
    }

}


class TransitiveVerbClause(
    val verb: Word,
    val subjectClause: NominalClause,
    val objectClause: NominalClause
): SyntaxClause {
    init {
        if (verb.semanticsCore.speechPart != SpeechPart.Verb)
            throw LanguageException("$verb is not a verb")
        if (verb.semanticsCore.tags.any { it.name == "intrans" })
            throw SyntaxException("$verb in the transitive clause is intransitive")
    }

    override fun toNode(language: Language): SentenceNode {
        val node = verb.toNode(language)
        val obj = objectClause.toNode(language)
        val subj = subjectClause.toNode(language)

        subj.setRelation(SyntaxRelation.Verb, node)
        obj.setRelation(SyntaxRelation.Verb, node)

        node.setRelation(SyntaxRelation.Subject, subj)
        node.setRelation(SyntaxRelation.Object, obj)

        return node
    }
}


internal fun Word.toNode(language: Language, presetCategories: List<CategoryValue> = listOf()): SentenceNode {
    val classNames = presetCategories
        .map { it.parentClassName }

    return SentenceNode(
        this,
        language.sentenceChangeParadigm.wordChangeParadigm
            .getDefaultState(this)
            .filter { it.source == CategorySource.SelfStated }
            .map { it.categoryValue }
            .filter { it.parentClassName !in classNames }
                + presetCategories
    )
}
