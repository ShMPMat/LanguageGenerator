package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.Phoneme


class PassingPhonemeSubstitution : PhonemeSubstitution {
    override fun substitute(word: Word, position: Int) =
        if (position < word.size && position >= 0) word[position]
        else throw LanguageException("Tried to change nonexistent phoneme on position $position in the word $word")

    override fun getSubstitutePhoneme(): Phoneme? = null

    override fun toString() = "_"
}
