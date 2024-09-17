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

    fun getPhonemeByPropertiesOrNull(phoneme: Phoneme): Phoneme? =
        phonemes.firstOrNull { it.isEqualByProperties(phoneme) }

    fun getPhonemeWithAddedModifiers(phoneme: Phoneme, modifiers: Set<PhonemeModifier>): Phoneme {
        modifiers.firstOrNull { it in phoneme.modifiers }
            ?.let {
                throw LanguageException("Can't add modifier '$it' to phoneme '$phoneme' already containing it")
            }

        val newPhoneme = phoneme.copy(modifiers = phoneme.modifiers + modifiers)
        val correctNamePhoneme = getPhonemeByPropertiesOrNull(newPhoneme)
            ?: throw LanguageException(
                "Phoneme with properties " +
                        "${newPhoneme.type}, " +
                        "${newPhoneme.articulationPlace}, " +
                        "${newPhoneme.articulationManner}, " +
                        "${newPhoneme.modifiers}, " +
                        "doesn't exist"
            )

        return correctNamePhoneme
    }

    fun getPhonemes(phonemeType: PhonemeType): List<Phoneme> =
        phonemes.filter { it.type == phonemeType }

    fun getPhonemesNot(phonemeType: PhonemeType): List<Phoneme> =
        phonemes.filter { it.type != phonemeType }
}
