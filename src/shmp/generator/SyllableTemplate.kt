package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.language.Phoneme
import shmp.language.Syllable
import kotlin.random.Random

interface SyllableTemplate{
    fun generateSyllable(
        phonemeContainer: PhonemeContainer,
        random: Random,
        isClosed: Boolean = false,
        shouldHaveInitial: Boolean = false,
        prefix: List<Syllable> = listOf()
    ): Syllable

    fun test(phonemes: List<Phoneme>): Boolean
}