package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.morphem.change.ChangeException
import io.tashtabash.lang.language.phonology.Phoneme


data class EpenthesisSubstitution(val epenthesisPhoneme: Phoneme) : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): List<Phoneme> =
        listOf(epenthesisPhoneme)

    override val isOriginalPhonemeChanged: Boolean = false

    override fun times(other: PhonemeSubstitution): PhonemeSubstitution =
        throw ChangeException("Can't combine EpenthesisSubstitution on another substitution")

    override fun toString() = "($epenthesisPhoneme)"
}
