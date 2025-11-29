package io.tashtabash.lang.generator

import io.tashtabash.lang.generator.util.SyllablePosition
import io.tashtabash.lang.generator.util.SyllableRestrictions
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.Syllable
import io.tashtabash.lang.language.phonology.SyllableValenceTemplate
import io.tashtabash.lang.language.phonology.ValencyPlace
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.testProbability


class SyllableValenceGenerator(val template: SyllableValenceTemplate) {
    private val ADD_TESTS = 10

//    private val syllableMapper = mutableMapOf<PhonemeContainer, Syllables>()

    private fun hasPhonemeTypeClash(previousSyllable: Syllable?, nextSyllable: Syllable?): Boolean =
        previousSyllable != null
                && nextSyllable != null
                && previousSyllable.phonemes.last().type == nextSyllable.phonemes[0].type

    fun generateSyllable(restrictions: SyllableRestrictions): Syllable {
        for (i in 1..ADD_TESTS) {
            val syllable = generateOneSyllable(restrictions)
            if (syllable.size != 1 || restrictions.prefix.isEmpty() || syllable != restrictions.prefix.last())
                if (!hasPhonemeTypeClash(restrictions.prefix.lastOrNull(), syllable))
                    return syllable
        }
        return generateOneSyllable(restrictions)
    }

    private fun generateOneSyllable(restrictions: SyllableRestrictions): Syllable {
        var syllable = Syllable(listOf(), 0)

        for (i in 1..ADD_TESTS) {
            val syllableValencies = chooseSyllableStructure(restrictions)

            val onset = makeSyllablePart(
                restrictions,
                { lst, p ->
                    testPhoneme(lst, p) && (lst.isEmpty()
                            || lst.last().articulationManner.sonorityLevel >= p.articulationManner.sonorityLevel)
                },
                syllableValencies.takeWhile { it.phonemeType != PhonemeType.Vowel }
            )
            val nucleus = makeSyllablePart(
                restrictions,
                this::testPhoneme,
                listOf(syllableValencies.first { it.phonemeType == PhonemeType.Vowel })
            )
            val coda = makeSyllablePart(
                restrictions,
                { lst, p ->
                    testPhoneme(lst, p) && (lst.isEmpty()
                            || lst.last().articulationManner.sonorityLevel <= p.articulationManner.sonorityLevel)
                },
                syllableValencies.takeLastWhile { it.phonemeType != PhonemeType.Vowel }
            )

            syllable = Syllable(onset + nucleus + coda, onset.size)

            if (syllable.size <= restrictions.phoneticRestrictions.avgWordLength)
                break
        }

        return syllable
    }

    private fun makeSyllablePart(
        restrictions: SyllableRestrictions,
        checker: (List<Phoneme>, Phoneme) -> Boolean,
        sequence: List<ValencyPlace>
    ): List<Phoneme> {
        val phonemes = ArrayList<Phoneme>()
        for (valency in sequence)
            for (i in 1..ADD_TESTS) {
                val phoneme = restrictions.phonemeContainer.getPhonemes(valency.phonemeType)
                    .randomElement()
                if (i == ADD_TESTS || checker(phonemes, phoneme)) {
                    phonemes += phoneme
                    break
                }
            }

        return phonemes
    }

    private fun chooseSyllableStructure(restrictions: SyllableRestrictions): List<ValencyPlace> {
        val syllable = mutableListOf(template.valencies[template.nucleusIndex])

        val compulsoryOnset = template.valencies.first().realizationProbability == 1.0
                && template.nucleusIndex != 0

        val startValencies = if (restrictions.hasInitial == true || compulsoryOnset) {
            syllable += template.valencies[template.nucleusIndex - 1]

            template.valencies.take(template.nucleusIndex - 1)
        } else template.valencies.take(template.nucleusIndex)

        if (restrictions.hasInitial != false)
            for (valency in startValencies.reversed())
                if (valency.realizationProbability.testProbability())
                    syllable += valency
                else break

        syllable.reverse()

        val endValencies = if (restrictions.hasFinal == true) {
            template.valencies.getOrNull(template.nucleusIndex + 1)?.let { syllable += it }

            template.valencies.drop(template.nucleusIndex + 2)
        } else template.valencies.drop(template.nucleusIndex + 1)

        if (restrictions.position == SyllablePosition.End && restrictions.hasFinal != false) {
            var shouldTest = true
            var lastType = template.valencies[template.nucleusIndex].phonemeType
            for (valency in endValencies) {
                shouldTest = shouldTest || lastType != valency.phonemeType
                lastType = valency.phonemeType
                if (shouldTest)
                    if (valency.realizationProbability.testProbability())
                        syllable += valency
                    else
                        shouldTest = false
            }
        }
        return syllable
    }

    private fun testPhoneme(phonemes: List<Phoneme>, phoneme: Phoneme): Boolean =
        phonemes.isEmpty() || phonemes.last() != phoneme
}
