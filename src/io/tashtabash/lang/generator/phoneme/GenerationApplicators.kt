package io.tashtabash.lang.generator.phoneme

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.PhonemePool
import io.tashtabash.lang.language.phonology.PhonemeType
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
            .randomElementOrNull { newVowel -> vowels.sumBy { newVowel.calculateDistance(it) }.toDouble() }

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
