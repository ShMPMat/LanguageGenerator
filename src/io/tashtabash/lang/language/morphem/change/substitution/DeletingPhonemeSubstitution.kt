package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.phonology.Phoneme


object DeletingPhonemeSubstitution : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): Phoneme? = null

    override fun times(other: PhonemeSubstitution): PhonemeSubstitution =
        this

    override fun toString() = "-"
}
