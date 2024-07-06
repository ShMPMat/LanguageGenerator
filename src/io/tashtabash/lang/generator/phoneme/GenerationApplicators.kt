package io.tashtabash.lang.generator.phoneme

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.PhonemePool
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.random.singleton.testProbability


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


abstract class ConsonantGenerationApplicator : GenerationApplicator {
    abstract fun changeConsonants(consonants: List<Phoneme>): List<Phoneme>

    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val consonants = phonemeContainer.getPhonemes(PhonemeType.Consonant)
        val changedConsonants = changeConsonants(consonants)

        return ImmutablePhonemeContainer(
            changedConsonants + phonemeContainer.getPhonemesNot(PhonemeType.Consonant)
        )
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


class AddRandomConsonantApplicator(private val phonemePool: PhonemePool) : ConsonantGenerationApplicator() {
    override fun changeConsonants(consonants: List<Phoneme>): List<Phoneme> {
        val newPhoneme = phonemePool.getPhonemes(PhonemeType.Consonant)
            .filter { it !in consonants }
            .randomElementOrNull { newVowel -> consonants.sumBy { calculateDistance(newVowel, it) }.toDouble() }

        if (newPhoneme != null)
            return consonants + listOf(newPhoneme)

        return consonants
    }
}


class AddRandomConsonantMannerRowApplicator(
    private val phonemePool: PhonemePool,
    private val gapProbability: Double = 0.2,
) : ConsonantGenerationApplicator() {
    override fun changeConsonants(consonants: List<Phoneme>): List<Phoneme> {
        val articulationManner = ArticulationManner.values()
            .filter { it.sonorityLevel > 0 }
            .randomElement()
        val newPhonemes = phonemePool.getPhonemes(PhonemeType.Consonant)
            .filter { it.articulationManner == articulationManner }
            .filter { it !in consonants }
            .filter { (1 - gapProbability).testProbability() }

        return consonants + newPhonemes
    }
}


class AddRandomConsonantPlaceRowApplicator(
    private val phonemePool: PhonemePool,
    private val gapProbability: Double = 0.2,
) : ConsonantGenerationApplicator() {
    override fun changeConsonants(consonants: List<Phoneme>): List<Phoneme> {
        val articulationPlace = consonantArticulationPlaces.randomElement()
        val newPhonemes = phonemePool.getPhonemes(PhonemeType.Consonant)
            .filter { it.articulationPlace == articulationPlace }
            .filter { it !in consonants }
            .filter { (1 - gapProbability).testProbability() }

        return consonants + newPhonemes
    }
}


object RemoveRandomConsonantApplicator : ConsonantGenerationApplicator() {
    override fun changeConsonants(consonants: List<Phoneme>): List<Phoneme> =
        consonants - consonants.randomElement()
}


class AddPhonemeApplicator(
    private val phonemePool: PhonemePool,
    private vararg val phonemeSymbols: String
) : GenerationApplicator {
    override fun apply(phonemeContainer: ImmutablePhonemeContainer): ImmutablePhonemeContainer {
        val newPhonemes = phonemeSymbols.map { symbol -> phonemePool.phonemes.first { it.symbol == symbol } }
        val phonemeSet = phonemeContainer.phonemes.toSet() + newPhonemes

        return ImmutablePhonemeContainer(phonemeSet.toList())
    }
}


fun GenerationApplicator.withRandomFeatureFilter(probability: Double) =
    RandomFeatureFilterApplicator(this, probability)
