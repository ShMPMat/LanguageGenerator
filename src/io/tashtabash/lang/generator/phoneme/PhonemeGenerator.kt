package io.tashtabash.lang.generator.phoneme

import io.tashtabash.lang.containers.PhonemePool
import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.VowelQualityAmount
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.*
import io.tashtabash.random.singleton.RandomSingleton.random
import kotlin.math.log2
import kotlin.math.pow


class PhonemeGenerator(private val phonemePool: PhonemePool) {
    private val MAX_VOWEL_QUALITIES = 20

    private val vowelApplicators = listOf<PhonemeGenerationCondition>(
        AddRandomVowelApplicator(phonemePool)
            .withProbability { 1.0 / log2(it.size.toDouble()) }
            .repeat { it.size >= 5 },
        RemoveRandomVowelApplicator
            .withProbability { (it.size - 2.0) / (MAX_VOWEL_QUALITIES - 1) }
            .repeat { true },
        VowelLengthApplicator
            .withRandomFeatureFilter(0.2)
            .withProbability { 1.0 - it.size.toDouble() / (MAX_VOWEL_QUALITIES + 1) },
        VowelNasalizationApplicator
            .withRandomFeatureFilter(0.3)
            .withProbability { 1.0 - (it.size.toDouble() / (MAX_VOWEL_QUALITIES * 2 + 1)).pow(0.2) },

        // Delete/add vowels again for more possible vowel system asymmetry.
        AddRandomVowelApplicator(phonemePool)
            .withProbability { 1.0 / log2(it.size.toDouble()) }
            .repeat { it.size >= 5 },
        RemoveRandomVowelApplicator
            .withProbability { (it.size - 2.0) / (MAX_VOWEL_QUALITIES - 1) }
            .repeat { true },
    )

    private fun generateStartVowels(vowelQualityAmount: VowelQualityAmount) = when (vowelQualityAmount) {
        VowelQualityAmount.Two -> listOf(
            listOf("e", "o"),
            listOf("a", "ɨ")
        )
        VowelQualityAmount.Three -> listOf(
            listOf("i", "u", "a"),
            listOf("i", "o", "a"),
            listOf("a", "e", "i")
        )
        VowelQualityAmount.Four -> listOf(
            listOf("i", "u", "ə", "a"),
            listOf("i", "u", "e", "a"),
            listOf("i", "ɪ", "e", "ɛ")// Large vertical system a-la Marshallese
        )
        VowelQualityAmount.Five -> listOf(
            listOf("i", "u", "e", "o", "a"),
            listOf("i", "u", "ə", "o", "a")
        )
        VowelQualityAmount.Six -> listOf(
            listOf("i", "u", "e", "ə", "o", "a"),
            listOf("i", "u", "e", "o", "æ", "a")
        )
        VowelQualityAmount.Seven -> listOf(
            listOf("i", "u", "e", "o", "ɛ", "ɔ", "a"),
            listOf("i", "y", "u", "e", "ø", "o", "a"),
            listOf("i", "ɨ", "u", "e", "ə", "o", "a")
        )
        VowelQualityAmount.Eight -> listOf(
            listOf("i", "u", "ɪ", "ʊ", "e", "o", "a", "ɔ"),
            listOf("i", "y", "ɯ", "u", "e", "ø", "a", "o")
        )
        VowelQualityAmount.Nine -> listOf(
            listOf("i", "u", "ɪ", "ʊ", "e", "o", "ɛ", "ɔ", "a"),
            listOf("i", "ɨ", "u", "e", "ə", "o", "ɛ", "ɔ", "a")
        )
        VowelQualityAmount.Ten -> listOf(
            listOf("i", "y", "u", "e", "ø", "o", "ɛ", "œ", "ɔ", "a"),
            listOf("i", "u", "ɪ", "ʊ", "e", "ə", "o", "ɛ", "a", "ɔ")
        )
    }
        .randomElement()
        .let { symbols -> ImmutablePhonemeContainer(phonemePool.phonemes.filter { it.symbol in symbols }) }

    private fun generateVowels(): List<Phoneme> {
        val vowelQualityAmount = VowelQualityAmount.values()
            .randomElement()
        var vowels = generateStartVowels(vowelQualityAmount)

        for (applicator in vowelApplicators)
            vowels = applicator.run(vowels) ?: vowels

        return vowels.phonemes
    }

    private fun generateConsonants(): List<Phoneme> {
        val consonantAmount = random.nextInt(6, 16)

        return randomSublist(
            phonemePool.getPhonemes(PhonemeType.Consonant),
            random,
            consonantAmount,
            consonantAmount + 1
        )
    }

    fun generate(): PhonemeContainer =
        ImmutablePhonemeContainer(generateVowels() + generateConsonants())
}
