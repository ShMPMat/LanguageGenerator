package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.phonology.Phoneme


object PassingPhonemeSubstitution : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): List<Phoneme> = listOfNotNull(phoneme)

    override fun times(other: PhonemeSubstitution): PhonemeSubstitution =
        other

    override fun toString() = "_"
}
