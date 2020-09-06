package shmp.language.syntax.clause

import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.category.CategorySource
import shmp.language.lexis.Word
import shmp.language.syntax.SentenceNode
import shmp.language.syntax.SyntaxException
import shmp.language.syntax.SyntaxRelation


interface SyntaxClause {
    fun toNode(language: Language): SentenceNode
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
