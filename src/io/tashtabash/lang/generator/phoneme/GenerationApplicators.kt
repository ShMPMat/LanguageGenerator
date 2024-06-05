package io.tashtabash.lang.generator.phoneme

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.PhonemePool
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.calculateDistance
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull


interface GenerationApplicator {
    fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer
}


class AddRandomVowelApplicator(val phonemePool: PhonemePool): GenerationApplicator {
    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val vowels = phonemeContainer.getPhonemes(PhonemeType.Vowel)

        val newPhoneme = phonemePool.getPhonemes(PhonemeType.Vowel)
            .filter { it !in vowels }
            .randomElementOrNull { newVowel -> vowels.sumBy { calculateDistance(newVowel, it) }.toDouble() }

        if (newPhoneme != null)
            return ImmutablePhonemeContainer(phonemeContainer.phonemes + listOf(newPhoneme))

        return phonemeContainer
    }
}


object RemoveRandomVowelApplicator: GenerationApplicator {
    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val vowels = phonemeContainer.getPhonemes(PhonemeType.Vowel)

        return ImmutablePhonemeContainer(phonemeContainer.phonemes - vowels.randomElement())
    }
}

object VowelLengthApplicator: GenerationApplicator {
    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val vowels = phonemeContainer.getPhonemes(PhonemeType.Vowel)
        val longVowels = vowels.map {
            it.copy(symbol = it.symbol + it.symbol, modifiers = it.modifiers + listOf(PhonemeModifier.Long))
        }

        return ImmutablePhonemeContainer(vowels + longVowels)
    }
}

object VowelNasalizationApplicator: GenerationApplicator {
    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val vowels = phonemeContainer.getPhonemes(PhonemeType.Vowel)
        val nasalizedVowels = vowels.map {
            it.copy(
                symbol = it.symbol.map { c -> c.toString() + '̃'  }.joinToString(""),
                modifiers = it.modifiers + listOf(PhonemeModifier.Nasalized)
            )
        }

        return ImmutablePhonemeContainer(vowels + nasalizedVowels)
    }
}
