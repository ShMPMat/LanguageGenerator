package shmp.generator

import shmp.containers.PhonemeBase
import shmp.containers.PhonemeContainer
import shmp.containers.PhonemeImmutableContainer
import shmp.language.PhonemeType
import shmp.language.VowelQualityAmount
import shmp.language.phonology.Phoneme
import shmp.random.randomSublist
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.RandomSingleton.random
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull

class PhonemeGenerator(val phonemeBase: PhonemeBase) {
    private val vowelAmount = VowelQualityAmount.values().randomElement().amount
    private val consonantAmount = RandomSingleton.random.nextInt(6, 16)

    fun generate(): PhonemeContainer {
        val extraVowels = phonemeBase.getPhonemesByType(PhonemeType.Vowel).drop(vowelAmount).toMutableList()
        val vowels = phonemeBase.getPhonemesByType(PhonemeType.Vowel)
            .take(vowelAmount)
            .mapIndexed { i, v ->
                (i / (vowelAmount + 1.0)).chanceOf<Phoneme> {
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

        return PhonemeImmutableContainer(vowels + consonants)
    }
}