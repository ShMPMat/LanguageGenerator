package io.tashtabash.lang.generator

import io.tashtabash.lang.containers.PhonemeBase
import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.VowelQualityAmount
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton.random
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import kotlin.math.pow


class PhonemeGenerator(private val phonemeBase: PhonemeBase) {
    private val vowelAmount = VowelQualityAmount.values().randomElement().amount
    private val consonantAmount = random.nextInt(6, 16)

    fun generate(): PhonemeContainer {
        val extraVowels = phonemeBase.getPhonemesByType(PhonemeType.Vowel)
            .drop(vowelAmount)
            .toMutableList()

        val vowels = phonemeBase.getPhonemesByType(PhonemeType.Vowel)
            .take(vowelAmount)
            .mapIndexed { i, v ->
                (i.toDouble().pow(0.5) / (vowelAmount + 1)).chanceOf<Phoneme> {
                    val newVowel = extraVowels.randomElementOrNull()
                        ?: return@chanceOf v
                    extraVowels.remove(newVowel)
                    newVowel
                } ?: v
            }

        val consonants = randomSublist(
            phonemeBase.getPhonemesByType(PhonemeType.Consonant),
            random,
            consonantAmount,
            consonantAmount + 1
        )

        return ImmutablePhonemeContainer(vowels + consonants)
    }
}