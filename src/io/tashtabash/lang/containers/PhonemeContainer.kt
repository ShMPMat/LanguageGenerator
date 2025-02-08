package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType


interface PhonemeContainer {
    val phonemes: List<Phoneme>

    val size: Int
        get() = phonemes.size

    fun getPhoneme(symbol: String): Phoneme =
        phonemes.firstOrNull { it.symbol == symbol }
            ?: throw NoPhonemeException("No phoneme with a symbol '$symbol' exist")

    fun getPhonemeOrNull(symbol: String): Phoneme? =
        phonemes.firstOrNull { it.symbol == symbol }

    fun getPhonemeByProperties(phoneme: Phoneme): Phoneme =
        getPhonemeByPropertiesOrNull(phoneme)
            ?: throw NoPhonemeException(
                "Phoneme with properties " +
                        "${phoneme.type}, " +
                        "${phoneme.articulationPlace}, ${phoneme.articulationManner}, " +
                        "${phoneme.modifiers}, " +
                        "doesn't exist"
            )

    fun getPhonemeByPropertiesOrNull(phoneme: Phoneme): Phoneme? =
        phonemes.firstOrNull { it.isEqualByProperties(phoneme) }

    fun getPhonemeWithShiftedModifiers(
        phoneme: Phoneme,
        addModifiers: Set<PhonemeModifier>,
        removeModifiers: Set<PhonemeModifier>
    ): Phoneme {
        val shiftedPhoneme = getPhonemeWithRemovedModifiersOrNull(phoneme, removeModifiers)
            ?: throw NoPhonemeException("Can't remove modifiers '$removeModifiers' from phoneme '$phoneme' w/o them")

        return getPhonemeWithAddedModifiersOrNull(shiftedPhoneme, addModifiers)
            ?: throw NoPhonemeException("Can't add modifiers '$addModifiers' to phoneme '$phoneme' already w/ them")
    }

    fun getPhonemeWithShiftedModifiersOrNull(
        phoneme: Phoneme,
        addModifiers: Set<PhonemeModifier>,
        removeModifiers: Set<PhonemeModifier>
    ): Phoneme? {
        val shiftedPhoneme = getPhonemeWithRemovedModifiersOrNull(phoneme, removeModifiers)
            ?: return null

        return getPhonemeWithAddedModifiersOrNull(shiftedPhoneme, addModifiers)
    }

    fun getPhonemeWithAddedModifiersOrNull(phoneme: Phoneme, modifiers: Set<PhonemeModifier>): Phoneme? {
        modifiers.firstOrNull { it in phoneme.modifiers }
            ?.let { return null }

        val newPhoneme = phoneme.copy(modifiers = phoneme.modifiers + modifiers)

        return getPhonemeByPropertiesOrNull(newPhoneme)
    }

    fun getPhonemeWithRemovedModifiersOrNull(phoneme: Phoneme, modifiers: Set<PhonemeModifier>): Phoneme? {
        modifiers.firstOrNull { it !in phoneme.modifiers }
            ?.let { return null }

        val newPhoneme = phoneme.copy(modifiers = phoneme.modifiers - modifiers)

        return getPhonemeByPropertiesOrNull(newPhoneme)
    }

    fun getPhonemes(phonemeType: PhonemeType): List<Phoneme> =
        phonemes.filter { it.type == phonemeType }

    fun getPhonemes(modifiers: Set<PhonemeModifier>): List<Phoneme> =
        phonemes.filter { it.modifiers.containsAll(modifiers) }

    fun getPhonemesNot(phonemeType: PhonemeType): List<Phoneme> =
        phonemes.filter { it.type != phonemeType }

    fun getPhonemesNot(modifiers: Set<PhonemeModifier>): List<Phoneme> =
        phonemes.filter { modifiers.none { m -> m in it.modifiers } }
}
