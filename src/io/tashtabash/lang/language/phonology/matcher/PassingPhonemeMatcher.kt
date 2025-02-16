package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme


object PassingPhonemeMatcher: PhonemeMatcher() {
    override val name =
        "*"

    override fun match(phoneme: Phoneme?) =
        true

    override fun match(changingPhoneme: ChangingPhoneme) =
        true

    override fun times(other: PhonemeMatcher?): Pair<PhonemeMatcher, Boolean>? = when (other) {
        BorderPhonemeMatcher -> null
        else -> (other ?: this) to false
    }
}
