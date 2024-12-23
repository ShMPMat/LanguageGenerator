package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.phonology.Phoneme


object DeletingPhonemeSubstitution : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): List<Phoneme> = listOf()

    override fun times(other: PhonemeSubstitution): PhonemeSubstitution =
        this

    override fun toString() = "-"
}
