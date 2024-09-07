package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.phonology.Phoneme


interface PhonemeSubstitution {
    fun substitute(phoneme: Phoneme?): Phoneme?
    fun getSubstitutePhoneme(): Phoneme?
}


fun createPhonemeSubstitution(substitution: String, phonemeContainer: PhonemeContainer) = when (substitution) {
    "-" -> DeletingPhonemeSubstitution
    "_" -> PassingPhonemeSubstitution
    else -> ExactPhonemeSubstitution(phonemeContainer.getPhoneme(substitution))
}
