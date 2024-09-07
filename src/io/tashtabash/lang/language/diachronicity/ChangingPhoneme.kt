package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.phonology.Phoneme


sealed class ChangingPhoneme {
    abstract val phoneme: Phoneme?

    data class ExactPhoneme(override val phoneme: Phoneme): ChangingPhoneme()

    object DeletedPhoneme: ChangingPhoneme() {
        override val phoneme: Phoneme? = null
    }

    object Boundary: ChangingPhoneme() {
        override val phoneme: Phoneme? = null
    }
}
