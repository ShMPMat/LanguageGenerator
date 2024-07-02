package io.tashtabash.lang.generator.phoneme

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.PhonemePool
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.calculateDistance
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull


interface GenerationApplicator {
    fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer
}


abstract class VowelGenerationApplicator : GenerationApplicator {
    abstract fun changeVowels(vowels: List<Phoneme>): List<Phoneme>

    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val vowels = phonemeContainer.getPhonemes(PhonemeType.Vowel)
        val changedVowels = changeVowels(vowels)

        return ImmutablePhonemeContainer(changedVowels + phonemeContainer.getPhonemesNot(PhonemeType.Vowel))
    }
}


class RandomFeatureFilterApplicator(
    private val applicator: GenerationApplicator,
    private val filterChance: Double
) : GenerationApplicator {
    private sealed class FeaturePresence<F>(val feature: F) {
        class Has<F>(feature: F) : FeaturePresence<F>(feature) {
            override fun filter(phonemes: List<Phoneme>, modifierPresencePredicate: (Phoneme, F) -> Boolean) =
                phonemes.filter { modifierPresencePredicate(it, feature) }
        }

        class None<F>(feature: F) : FeaturePresence<F>(feature) {
            override fun filter(phonemes: List<Phoneme>, modifierPresencePredicate: (Phoneme, F) -> Boolean) =
                phonemes.filter { !modifierPresencePredicate(it, feature) }
        }

        abstract fun filter(phonemes: List<Phoneme>, modifierPresencePredicate: (Phoneme, F) -> Boolean): List<Phoneme>
    }

    private fun <E> createPresenceOptions(feature: E): List<FeaturePresence<E>> =
        listOf(FeaturePresence.Has(feature), FeaturePresence.None(feature))

    private fun <F> chooseFilterValue(rawFeatures: List<F>): F? {
        (1 - filterChance).chanceOf {
            return null
        }

        return rawFeatures.distinct()
            .randomElementOrNull()
    }

    private fun filterPhonemes(phonemeContainer: ImmutablePhonemeContainer): List<Phoneme> {
        var resultPhonemes = phonemeContainer.phonemes

        chooseFilterValue(
            phonemeContainer.phonemes.flatMap { createPresenceOptions(it.articulationPlace) }
        )?.let { articulationPlaceCondition ->
            resultPhonemes = articulationPlaceCondition.filter(resultPhonemes) { phoneme, articulationPlace ->
                phoneme.articulationPlace == articulationPlace
            }
        }

        chooseFilterValue(
            phonemeContainer.phonemes.flatMap { createPresenceOptions(it.articulationManner) }
        )?.let { articulationMannerCondition ->
            resultPhonemes = articulationMannerCondition.filter(resultPhonemes) { phoneme, articulationManner ->
                phoneme.articulationManner == articulationManner
            }
        }

        val possibleModifiers = phonemeContainer.phonemes
            .flatMap { it.modifiers }
            .distinct()
        for (possibleModifier in possibleModifiers)
            chooseFilterValue(createPresenceOptions(possibleModifier))
                ?.let { modifierCondition ->
                    resultPhonemes = modifierCondition.filter(resultPhonemes) { phoneme, feature ->
                        feature in phoneme.modifiers
                    }
                }

        return resultPhonemes
    }

    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val chosenPhonemes = filterPhonemes(phonemeContainer)
        val excludedPhonemes = phonemeContainer.phonemes.filter { it !in chosenPhonemes }
        val applicatorResult = applicator.apply(ImmutablePhonemeContainer(chosenPhonemes))

        return ImmutablePhonemeContainer(excludedPhonemes + applicatorResult.phonemes)
    }
}


class AddRandomVowelApplicator(private val phonemePool: PhonemePool) : VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> {
        val newPhoneme = phonemePool.getPhonemes(PhonemeType.Vowel)
            .filter { it !in vowels }
            .randomElementOrNull { newVowel -> vowels.sumBy { calculateDistance(newVowel, it) }.toDouble() }

        if (newPhoneme != null)
            return vowels + listOf(newPhoneme)

        return vowels
    }
}


object RemoveRandomVowelApplicator : VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> =
        vowels - vowels.randomElement()
}


object VowelLengthApplicator : VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> {
        val longVowels = vowels.map {
            it.copy(symbol = it.symbol + it.symbol, modifiers = it.modifiers + listOf(PhonemeModifier.Long))
        }

        return vowels + longVowels
    }
}


object VowelNasalizationApplicator : VowelGenerationApplicator() {
    override fun changeVowels(vowels: List<Phoneme>): List<Phoneme> {
        val nasalizedVowels = vowels.map {
            it.copy(
                symbol = it.symbol.map { c -> c.toString() + '̃' }.joinToString(""),
                modifiers = it.modifiers + listOf(PhonemeModifier.Nasalized)
            )
        }

        return vowels + nasalizedVowels
    }
}


fun GenerationApplicator.withRandomFeatureFilter(probability: Double) =
    RandomFeatureFilterApplicator(this, probability)
