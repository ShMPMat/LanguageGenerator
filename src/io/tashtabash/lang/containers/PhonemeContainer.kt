package io.tashtabash.lang.containers

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType


interface PhonemeContainer {
    val phonemes: List<Phoneme>

    val size: Int
        get() = phonemes.size

    fun getPhoneme(symbol: String): Phoneme =
        phonemes.first { it.symbol == symbol }

    fun getPhonemeOrNull(symbol: String): Phoneme? =
        phonemes.firstOrNull { it.symbol == symbol }

    fun getPhonemeByProperties(phoneme: Phoneme): Phoneme =
        getPhonemeByPropertiesOrNull(phoneme)
            ?: throw NoPhonemeException(
                "Phoneme with properties " +
                        "${phoneme.type}, " +
                        "${phoneme.articulationPlace}, " +
                        "${phoneme.articulationManner}, " +
                        "${phoneme.modifiers}, " +
                        "doesn't exist"
            )

    fun getPhonemeByPropertiesOrNull(phoneme: Phoneme): Phoneme? =
        phonemes.firstOrNull { it.isEqualByProperties(phoneme) }

    fun getPhonemeWithAddedModifiers(phoneme: Phoneme, modifiers: Set<PhonemeModifier>): Phoneme {
        modifiers.firstOrNull { it in phoneme.modifiers }
            ?.let {
                throw LanguageException("Can't add modifier '$it' to phoneme '$phoneme' already containing it")
            }

        val newPhoneme = phoneme.copy(modifiers = phoneme.modifiers + modifiers)

        return getPhonemeByProperties(newPhoneme)
    }

    fun getPhonemeWithRemovedModifiers(phoneme: Phoneme, modifiers: Set<PhonemeModifier>): Phoneme {
        modifiers.firstOrNull { it !in phoneme.modifiers }
            ?.let {
                throw LanguageException("Can't remove modifiers '$modifiers' from phoneme '$phoneme' without them")
            }

        val newPhoneme = phoneme.copy(modifiers = phoneme.modifiers - modifiers)

        return getPhonemeByProperties(newPhoneme)
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
