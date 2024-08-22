package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.phonology.Phoneme


class ExactPhonemeSubstitution(val exactPhoneme: Phoneme) : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): Phoneme = exactPhoneme

    override fun getSubstitutePhoneme() = exactPhoneme

    override fun toString() = exactPhoneme.toString()
}
