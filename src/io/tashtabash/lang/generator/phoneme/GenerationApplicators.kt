package io.tashtabash.lang.generator.phoneme

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.PhonemePool
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.calculateDistance
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull


interface GenerationApplicator {
    fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer
}


abstract class VowelGenerationApplicator: GenerationApplicator {
    abstract fun changeVowels(vowels: List<Phoneme>): List<Phoneme>

    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val vowels = phonemeContainer.getPhonemes(PhonemeType.Vowel)
        val changedVowels = changeVowels(vowels)

        return ImmutablePhonemeContainer(changedVowels + phonemeContainer.getPhonemesNot(PhonemeType.Vowel))
    }
}


class AddRandomVowelApplicator(private val phonemePool: PhonemePool): VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> {
        val newPhoneme = phonemePool.getPhonemes(PhonemeType.Vowel)
            .filter { it !in vowels }
            .randomElementOrNull { newVowel -> vowels.sumBy { calculateDistance(newVowel, it) }.toDouble() }

        if (newPhoneme != null)
            return vowels + listOf(newPhoneme)

        return vowels
    }
}


object RemoveRandomVowelApplicator: VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> =
        vowels - vowels.randomElement()
}


object VowelLengthApplicator: VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> {
        val longVowels = vowels.map {
            it.copy(symbol = it.symbol + it.symbol, modifiers = it.modifiers + listOf(PhonemeModifier.Long))
        }

        return vowels + longVowels
    }
}


object VowelNasalizationApplicator: VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> {
        val nasalizedVowels = vowels.map {
            it.copy(
                symbol = it.symbol.map { c -> c.toString() + '̃'  }.joinToString(""),
                modifiers = it.modifiers + listOf(PhonemeModifier.Nasalized)
            )
        }

        return vowels + nasalizedVowels
    }
}
