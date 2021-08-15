package shmp.lang.language.morphem.change.substitution

import shmp.lang.language.LanguageException
import shmp.lang.language.lexis.Word
import shmp.lang.language.phonology.Phoneme


class PassingPositionSubstitution : PositionSubstitution {
    override fun substitute(word: Word, position: Int) =
        if (position < word.size && position >= 0) word[position]
        else throw LanguageException("Tried to change nonexistent phoneme on position $position in the word $word")

    override fun getSubstitutePhoneme(): Phoneme? = null

    override fun toString() = "_"
}
