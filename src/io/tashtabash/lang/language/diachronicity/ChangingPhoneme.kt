package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.prosody.Prosody


sealed class ChangingPhoneme {
    abstract val phoneme: Phoneme?
    abstract val prosody: List<Prosody>?

    data class ExactPhoneme(
        override val phoneme: Phoneme,
        override val prosody: List<Prosody>? = null
    ): ChangingPhoneme()

    object DeletedPhoneme: ChangingPhoneme() {
        override val phoneme: Phoneme? = null
        override val prosody: List<Prosody>? = null
    }

    object Boundary: ChangingPhoneme() {
        override val phoneme: Phoneme? = null
        override val prosody: List<Prosody>? = null
    }
}


fun clearChangingPhonemes(phonemes: List<ChangingPhoneme>): List<Phoneme> =
    phonemes.filterIsInstance<ChangingPhoneme.ExactPhoneme>()
        .map { it.phoneme }

fun getChangingPhonemes(
    word: Word,
    addStartBoundary: Boolean = true,
    addEndBoundary: Boolean = true
): MutableList<ChangingPhoneme> =
    getChangingPhonemes(word.toPhonemes().zip(getProsodyContour(word)), addStartBoundary, addEndBoundary)

fun getChangingPhonemes(
    phonemes: List<Pair<Phoneme, List<Prosody>?>>,
    addStartBoundary: Boolean,
    addEndBoundary: Boolean
): MutableList<ChangingPhoneme> {
    val rawPhonemes = mutableListOf<ChangingPhoneme>()

    if (addStartBoundary)
        rawPhonemes += listOf(ChangingPhoneme.Boundary)
    rawPhonemes += phonemes.map { (phoneme, prosody) -> ChangingPhoneme.ExactPhoneme(phoneme, prosody) }
    if (addEndBoundary)
        rawPhonemes += listOf(ChangingPhoneme.Boundary)

    return rawPhonemes
}

private fun getProsodyContour(word: Word): List<List<Prosody>?> {
    return word.syllables.flatMap { syllable ->
        val prefix = (0 until syllable.nucleusIdx).map<Int, List<Prosody>?> { null }
        val postfix = (syllable.nucleusIdx + 1 until syllable.size).map<Int, List<Prosody>?> { null }

        prefix + listOf(syllable.prosody) + postfix
    }
}
