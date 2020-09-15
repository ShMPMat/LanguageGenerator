package shmp.language.lexis

import shmp.language.LanguageException


class Lexis(val words: List<Word>, val copula: Word?) {
    val size: Int
        get() = words.size

    fun getWord(meaning: Meaning) = getWordOrNull(meaning)
        ?: throw LanguageException("No word with meaning '$meaning' in Language")

    fun getWordOrNull(meaning: Meaning) = words
        .firstOrNull { it.semanticsCore.hasMeaning(meaning) }

    override fun toString() = """
        |word roots:
        |${words.joinToString { it.toString() + " - " + it.semanticsCore }}
        |copula: $copula
    """.trimMargin()
}
