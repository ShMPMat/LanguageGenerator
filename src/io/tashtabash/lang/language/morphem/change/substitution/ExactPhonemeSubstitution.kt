package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.phonology.Phoneme


data class ExactPhonemeSubstitution(val exactPhoneme: Phoneme) : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): List<Phoneme> = listOf(exactPhoneme)

    override fun times(other: PhonemeSubstitution): PhonemeSubstitution =
        this

    override fun toString() = exactPhoneme.toString()
}
