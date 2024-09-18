package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.lexis.Word
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


fun clearChangingPhonemes(phonemes: List<ChangingPhoneme>): List<Phoneme> =
    phonemes.filterIsInstance<ChangingPhoneme.ExactPhoneme>()
        .map { it.phoneme }

fun getChangingPhonemes(word: Word): MutableList<ChangingPhoneme> =
    getChangingPhonemes(word.toPhonemes(), addStartBoundary = true, addEndBoundary = true)

fun getChangingPhonemes(
    phonemes: List<Phoneme>,
    addStartBoundary: Boolean,
    addEndBoundary: Boolean
): MutableList<ChangingPhoneme> {
    val rawPhonemes = mutableListOf<ChangingPhoneme>()

    if (addStartBoundary)
        rawPhonemes += listOf(ChangingPhoneme.Boundary)
    rawPhonemes += phonemes.map { ChangingPhoneme.ExactPhoneme(it) }
    if (addEndBoundary)
        rawPhonemes += listOf(ChangingPhoneme.Boundary)

    return rawPhonemes
}
