package shmp.language.syntax

import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.category.CategorySource
import shmp.language.lexis.Word


interface SyntaxConstruction {
    fun toNode(language: Language, presetCategories: List<CategoryValue> = listOf()): SentenceNode
}


abstract class NounDefiner: SyntaxConstruction

class AdjectiveDefiner(val adjective: Word): NounDefiner() {
    init {
        if (adjective.semanticsCore.speechPart != SpeechPart.Adjective)
            throw LanguageException("$adjective is not an adjective")
    }

    override fun toNode(language: Language, presetCategories: List<CategoryValue>) = adjective.toNode(
        language,
        presetCategories
    )
}


class NominalClause(val noun: Word, val definitions: List<NounDefiner>): SyntaxConstruction {
    override fun toNode(language: Language, presetCategories: List<CategoryValue>): SentenceNode {
        val definers = definitions
//            .map { it.toNode(language) }
//            .forEach { it.setRelation() }
        TODO()
    }

}


fun Word.toNode(language: Language, presetCategories: List<CategoryValue> = listOf()): SentenceNode {
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
