package shmp.lang.generator

import shmp.lang.generator.util.SyllablePosition
import shmp.lang.generator.util.SyllableRestrictions
import shmp.lang.language.phonology.PhonemeType
import shmp.lang.language.phonology.Phoneme
import shmp.lang.language.phonology.Syllable
import shmp.lang.language.phonology.SyllableValenceTemplate
import shmp.lang.language.phonology.ValencyPlace
import shmp.random.randomElement
import shmp.random.testProbability
import kotlin.random.Random


class SyllableValenceGenerator(val template: SyllableValenceTemplate) {
    private val ADD_TESTS = 10

//    private val syllableMapper = mutableMapOf<PhonemeContainer, List<Syllable>>()

    fun generateSyllable(
        restrictions: SyllableRestrictions,
        random: Random
    ): Syllable {
        for (i in 0 until ADD_TESTS) {
            val syllable = generateOneSyllable(restrictions, random)
            if (syllable.size != 1 || restrictions.prefix.isEmpty() || syllable != restrictions.prefix.last())
                if ((!restrictions.shouldHaveInitial || syllable[0].type == PhonemeType.Consonant)
                    && restrictions.prefix
                        .lastOrNull()
                        ?.let { it.phonemeSequence.last().type != syllable.phonemeSequence[0].type } != false
                )
                //if (!shouldHaveFinal)
                    return syllable
        }
        return generateOneSyllable(restrictions, random)
    }

    private fun generateOneSyllable(
        restrictions: SyllableRestrictions,
        random: Random
    ): Syllable {
        var syllable = Syllable(listOf())

        for (i in 1..ADD_TESTS) {
            val actualSyllable = chooseSyllableStructure(restrictions, random)

            val onset = makeSyllablePart(
                restrictions,
                { lst, p ->
                    testPhoneme(lst, p) && (lst.isEmpty()
                            || lst.last().articulationManner.sonorityLevel >= p.articulationManner.sonorityLevel)
                },
                actualSyllable.takeWhile { it.phonemeType != PhonemeType.Vowel },
                random
            )
            val nucleus = makeSyllablePart(
                restrictions,
                this::testPhoneme,
                listOf(actualSyllable.first { it.phonemeType == PhonemeType.Vowel }),
                random
            )
            val coda = makeSyllablePart(
                restrictions,
                { lst, p ->
                    testPhoneme(lst, p) && (lst.isEmpty()
                            || lst.last().articulationManner.sonorityLevel <= p.articulationManner.sonorityLevel)
                },
                actualSyllable.takeLastWhile { it.phonemeType != PhonemeType.Vowel },
                random
            )

            syllable = Syllable(onset + nucleus + coda)

            if (syllable.size <= restrictions.phoneticRestrictions.avgWordLength)
                break
        }

        return syllable
    }

    private fun makeSyllablePart(
        restrictions: SyllableRestrictions,
        checker: (List<Phoneme>, Phoneme) -> Boolean,
        sequence: List<ValencyPlace>,
        random: Random
    ): List<Phoneme> {
        val phonemes = ArrayList<Phoneme>()
        for (valency in sequence) {
            for (i in 1..ADD_TESTS) {
                val phoneme = randomElement(
                    restrictions.phonemeContainer.getPhonemesByType(valency.phonemeType),
                    random
                )
                if (i == ADD_TESTS)
                    phonemes.add(phoneme)
                else if (checker(phonemes, phoneme)) {
                    phonemes.add(phoneme)
                    break
                }
            }
        }
        return phonemes
    }

    private fun chooseSyllableStructure(
        restrictions: SyllableRestrictions,
        random: Random
    ): List<ValencyPlace> {
        val syllable = ArrayList<ValencyPlace>()
        for (valency in template.valencies.take(template.nucleusIndex + 1).reversed())
            if (testProbability(valency.realizationProbability, random))
                syllable.add(valency)
            else
                break
        syllable.reverse()

        if (restrictions.position == SyllablePosition.End) {
            var shouldTest = true
            var lastType = template.valencies[template.nucleusIndex].phonemeType
            for (valency in template.valencies.drop(template.nucleusIndex + 1)) {
                shouldTest = shouldTest || lastType != valency.phonemeType
                lastType = valency.phonemeType
                if (shouldTest)
                    if (testProbability(valency.realizationProbability, random))
                        syllable.add(valency)
                    else
                        shouldTest = false
            }
        }
        return syllable
    }

    private fun testPhoneme(phonemes: List<Phoneme>, phoneme: Phoneme): Boolean {
        if (phonemes.isNotEmpty() && phonemes.last() == phoneme)
            return false
        return true
    }

//    private fun fillSyllables(phonemeContainer: PhonemeContainer) {
//        val allSyllables = mutableListOf<Syllable>()
//        val nuclei = phonemeContainer.getPhonemesByType(PhonemeType.Vowel)
//            .map { listOf(it) }
//
//        val maxOnsetSize = template.valencies
//            .takeWhile { it.phonemeType != PhonemeType.Vowel }
//            .size
//        val maxOffsetSize = template.valencies
//            .takeLastWhile { it.phonemeType != PhonemeType.Vowel }
//            .size
//
//        val maxOnsets = makeAllSequences(phonemeContainer, maxOnsetSize)
//            .filter {
//                for (i in it.indices.drop(1)) {
//                    val c = it[i]
//                    val p = it[i - 1]
//                    if (c == p || c.articulationManner.sonorityLevel < p.articulationManner.sonorityLevel)
//                }
//                return@filter true
//            }
//
//
//        syllableMapper[phonemeContainer] = allSyllables
//    }
//
//    private fun makeAllSequences(phonemeContainer: PhonemeContainer, size: Int): List<List<Phoneme>> =
//        (1..size)
//            .map { phonemeContainer.getPhonemesByType(PhonemeType.Consonant) }
//            .cartesianProduct()
}
