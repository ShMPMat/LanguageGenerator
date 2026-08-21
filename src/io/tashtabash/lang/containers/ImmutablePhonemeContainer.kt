package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.Phoneme


data class ImmutablePhonemeContainer(override val phonemes: List<Phoneme>) : PhonemeContainer {
    override val phonemesByProperties: Map<Phoneme, Phoneme> by lazy {
        phonemes.associateBy { it.copy(symbol = "_") }
    }

    override fun toString() = phonemes.groupBy { it.type }
        .entries
        .joinToString { (type, phonemes: List<Phoneme>) ->
            val sortedPhonemes = phonemes.sortedWith(
                compareBy<Phoneme> { it.articulationPlace }
                    .thenBy { it.articulationManner }
                    .thenBy { it.modifiers.size }
            )

            "$type (${sortedPhonemes.size}): $sortedPhonemes"
        }
}
