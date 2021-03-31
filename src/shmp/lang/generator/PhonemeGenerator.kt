package shmp.lang.generator

import shmp.lang.containers.PhonemeBase
import shmp.lang.containers.PhonemeContainer
import shmp.lang.containers.PhonemeImmutableContainer
import shmp.lang.language.phonology.PhonemeType
import shmp.lang.language.VowelQualityAmount
import shmp.lang.language.phonology.Phoneme
import shmp.random.randomSublist
import shmp.random.singleton.RandomSingleton.random
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
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

        return PhonemeImmutableContainer(vowels + consonants)
    }
}