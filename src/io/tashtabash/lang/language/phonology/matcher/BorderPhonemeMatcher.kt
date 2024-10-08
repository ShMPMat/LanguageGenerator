package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme


object BorderPhonemeMatcher: PhonemeMatcher() {
    override val name =
        "$"

    override fun match(phoneme: Phoneme?) =
        phoneme == null

    override fun match(changingPhoneme: ChangingPhoneme) =
        changingPhoneme == ChangingPhoneme.Boundary

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        BorderPhonemeMatcher, null -> this
        else -> null
    }
}
