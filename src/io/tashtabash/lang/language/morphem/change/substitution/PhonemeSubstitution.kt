package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.Phoneme


interface PhonemeSubstitution {
    fun substitute(word: Word, position: Int): Phoneme
    fun getSubstitutePhoneme(): Phoneme?
}
