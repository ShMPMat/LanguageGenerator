package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier


data class AddModifierPhonemeSubstitution(
    val modifiers: Set<PhonemeModifier>,
    val phonemes: PhonemeContainer
) : PhonemeSubstitution {
    override fun substitute(phoneme: Phoneme?): Phoneme? {
        phoneme
            ?: return null

        return phonemes.getPhonemeWithAddedModifiers(phoneme, modifiers)
    }

    override fun getSubstitutePhoneme(): Phoneme? = null

    override fun toString() = "[+${modifiers.joinToString(",")}]"
}
