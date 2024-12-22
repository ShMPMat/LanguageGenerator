package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier


data class ModifierPhonemeSubstitution(
    val addModifiers: Set<PhonemeModifier>,
    val removeModifiers: Set<PhonemeModifier>,
    val phonemes: PhonemeContainer
) : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): Phoneme? {
        phoneme
            ?: return null

        return phonemes.getPhonemeWithShiftedModifiers(phoneme, addModifiers, removeModifiers)
    }

    override fun times(other: PhonemeSubstitution): PhonemeSubstitution = when (other) {
        is PassingPhonemeSubstitution -> this
        is ModifierPhonemeSubstitution -> {
            val result = ModifierPhonemeSubstitution(
                other.addModifiers + addModifiers - removeModifiers,
                other.removeModifiers + removeModifiers - addModifiers,
                phonemes
            )

            if (result.removeModifiers.isEmpty() && result.addModifiers.isEmpty())
                PassingPhonemeSubstitution
            else
                result
        }
        is ExactPhonemeSubstitution -> {
            val newPhoneme = phonemes.getPhonemeWithShiftedModifiers(other.exactPhoneme, addModifiers, removeModifiers)
            ExactPhonemeSubstitution(newPhoneme)
        }
        else -> throw LanguageException("Unknown PhonemeSubstitution type '${other.javaClass.name}'")
    }

    override fun toString() = "[" +
            (if (addModifiers.isEmpty()) "" else "+") +
            addModifiers.joinToString(",") +
            (if (addModifiers.isEmpty() || removeModifiers.isEmpty()) "" else ",") +
            (if (removeModifiers.isEmpty()) "" else "-") +
            removeModifiers.joinToString(",") +
            "]"
}
