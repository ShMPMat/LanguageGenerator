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
    override fun substitute(phoneme: Phoneme?): List<Phoneme> {
        val newPhoneme = phoneme?.let { phonemes.getPhonemeWithShiftedModifiers(it, addModifiers, removeModifiers) }

        return listOfNotNull(newPhoneme)
    }

    fun substituteOrNull(phoneme: Phoneme?): Phoneme? =
        phoneme?.let { phonemes.getPhonemeWithShiftedModifiersOrNull(it, addModifiers, removeModifiers) }

    override fun times(other: PhonemeSubstitution): PhonemeSubstitution? = when (other) {
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
        is ExactPhonemeSubstitution ->
            phonemes.getPhonemeWithShiftedModifiersOrNull(other.exactPhoneme, addModifiers, removeModifiers)
                ?.let { ExactPhonemeSubstitution(it) }
        is EpenthesisSubstitution ->
            phonemes.getPhonemeWithShiftedModifiersOrNull(other.epenthesisPhoneme, addModifiers, removeModifiers)
                ?.let { EpenthesisSubstitution(it) }
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
