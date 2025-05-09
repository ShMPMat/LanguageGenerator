package io.tashtabash.lang.containers

import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeCharacteristic
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher


interface PhonemeContainer {
    val phonemes: List<Phoneme>

    val size: Int
        get() = phonemes.size

    fun getPhoneme(symbol: String): Phoneme =
        phonemes.firstOrNull { it.symbol == symbol }
            ?: throw NoPhonemeException("No phoneme with a symbol '$symbol' exist")

    fun getPhonemes(vararg symbols: String): List<Phoneme> =
        phonemes.filter { it.symbol in symbols }
            .also {
                if (it.size != symbols.size)
                    throw NoPhonemeException("Some phonemes with symbols '${symbols.joinToString()}' don't exist")
            }

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
            ?: throw NoPhonemeException("Can't remove modifiers '$removeModifiers' from phoneme '$phoneme'")

        return getPhonemeWithAddedModifiersOrNull(shiftedPhoneme, addModifiers)
            ?: throw NoPhonemeException("Can't add modifiers '$addModifiers' to phoneme '$phoneme'")
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

    fun getPhonemes(phonemeMatcher: PhonemeMatcher): List<Phoneme> =
        phonemes.filter { phonemeMatcher.match(it) }

    fun getPhonemes(characteristic: PhonemeCharacteristic): List<Phoneme> =
        phonemes.filter { it.characteristics.contains(characteristic) }

    fun getPhonemes(characteristics: Set<PhonemeCharacteristic>): List<Phoneme> =
        phonemes.filter { it.characteristics.containsAll(characteristics) }

    fun getPhonemesNot(phonemeType: PhonemeType): List<Phoneme> =
        phonemes.filter { it.type != phonemeType }

    fun getPhonemesNot(phonemeMatcher: PhonemeMatcher): List<Phoneme> =
        phonemes.filter { !phonemeMatcher.match(it) }

    fun getPhonemesNot(characteristic: PhonemeCharacteristic): List<Phoneme> =
        phonemes.filter { !it.characteristics.contains(characteristic) }

    fun getPhonemesNot(characteristics: Set<PhonemeCharacteristic>): List<Phoneme> =
        phonemes.filter { characteristics.none { m -> m in it.modifiers } }
}
