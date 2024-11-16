package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.LanguageException


abstract class AbstractLexis {
    abstract val words: List<Word>

    fun getWordOrNull(meaning: Meaning) = words
        .firstOrNull { it.semanticsCore.hasMeaning(meaning) }

    fun getWord(meaning: Meaning) = getWordOrNull(meaning)
        ?: throw LanguageException("No word with meaning '$meaning' in Language")
}
