package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.Phoneme


interface PositionSubstitution {
    fun substitute(word: Word, position: Int): Phoneme
    fun getSubstitutePhoneme(): Phoneme?
}
