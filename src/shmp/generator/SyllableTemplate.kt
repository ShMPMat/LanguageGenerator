package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.language.*
import kotlin.random.Random

interface SyllableTemplate{
    fun generateSyllable(
        phonemeContainer: PhonemeContainer,
        random: Random,
        canHaveFinal: Boolean = false,
        shouldHaveInitial: Boolean = false,
        shouldHaveFinal: Boolean = false,
        prefix: List<Syllable> = listOf()
    ): Syllable

    fun test(phonemes: PhonemeSequence): Boolean

    fun createWord(phonemes: PhonemeSequence, syntaxCore: SyntaxCore): Word?
}