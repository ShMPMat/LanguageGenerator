package shmp.lang.language.morphem.change.substitution

import shmp.lang.language.lexis.Word
import shmp.lang.language.phonology.Phoneme


class PhonemePositionSubstitution(val phoneme: Phoneme) :
    PositionSubstitution {
    override fun substitute(word: Word, position: Int) = phoneme
    override fun getSubstitutePhoneme() = phoneme

    override fun toString() = phoneme.toString()
}
