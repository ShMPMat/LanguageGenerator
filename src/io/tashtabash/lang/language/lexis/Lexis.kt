package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.language.syntax.features.QuestionMarker


data class Lexis(
    val words: List<Word>,
    private val copula: WordMap<CopulaType>,
    val questionMarker: WordMap<QuestionMarker>
) {
    val size: Int
        get() = words.size

    fun getWord(meaning: Meaning) = getWordOrNull(meaning)
        ?: throw LanguageException("No word with meaning '$meaning' in Language")

    fun getBySpeechPart(speechPart: TypedSpeechPart) = words
        .filter { it.semanticsCore.speechPart == speechPart }

    fun getCopulaWord(type: CopulaType) = copula[type]
        ?: throw LanguageException("No copula in Language for type $type")

    fun getQuestionMarkerWord(type: QuestionMarker) = questionMarker[type]
        ?: throw LanguageException("No question marker in Language for type $type")

    fun getWordOrNull(meaning: Meaning) = words
        .firstOrNull { it.semanticsCore.hasMeaning(meaning) }

    override fun toString() = """
        |copula: $copula
        |question marker: $questionMarker
        |word roots:
        |${words.joinToString { it.toString() + " - " + it.semanticsCore }}
    """.trimMargin()
}

typealias WordMap<E> = Map<E, Word>
