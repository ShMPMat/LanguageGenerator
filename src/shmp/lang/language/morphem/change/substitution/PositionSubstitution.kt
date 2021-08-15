package shmp.lang.language.morphem.change.substitution

import shmp.lang.language.lexis.Word
import shmp.lang.language.phonology.Phoneme


interface PositionSubstitution {
    fun substitute(word: Word, position: Int): Phoneme
    fun getSubstitutePhoneme(): Phoneme?
}
