package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.ArticulationPlace
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType
import java.util.Comparator


data class ImmutablePhonemeContainer(override val phonemes: List<Phoneme>) : PhonemeContainer {
    override fun toString() = phonemes.groupBy { it.type }
        .entries
        .joinToString { (type, phonemes) ->
            val sortedPhonemes = phonemes
                .sortedWith(Comparator.comparing<Phoneme?, ArticulationPlace?> { it.articulationPlace }
                .then(Comparator.comparing { it.articulationManner })
                .then(Comparator.comparing { it.modifiers.size }))

            "$type: $sortedPhonemes"
        }
}
