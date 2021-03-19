package shmp.language.lexis

import shmp.language.LanguageException
import shmp.language.syntax.features.CopulaType


class Lexis(val words: List<Word>, private val copula: Map<CopulaType, Word>) {
    val size: Int
        get() = words.size

    fun getWord(meaning: Meaning) = getWordOrNull(meaning)
        ?: throw LanguageException("No word with meaning '$meaning' in Language")

    fun getCopulaWord(type: CopulaType) = copula[type]
        ?: throw LanguageException("No copula in Language for type $type")

    fun getWordOrNull(meaning: Meaning) = words
        .firstOrNull { it.semanticsCore.hasMeaning(meaning) }

    override fun toString() = """
        |word roots:
        |${words.joinToString { it.toString() + " - " + it.semanticsCore }}
        |copula: $copula
    """.trimMargin()
}
