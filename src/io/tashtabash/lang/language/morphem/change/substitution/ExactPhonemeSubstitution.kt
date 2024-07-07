package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.Phoneme


class ExactPhonemeSubstitution(val phoneme: Phoneme) :
    PhonemeSubstitution {
    override fun substitute(word: Word, position: Int) = phoneme
    override fun getSubstitutePhoneme() = phoneme

    override fun toString() = phoneme.toString()
}
