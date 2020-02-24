package shmp.language.phonology

import shmp.containers.PhonemeContainer
import shmp.language.*
import shmp.language.phonology.PhonemeSequence
import shmp.language.phonology.Syllable
import kotlin.random.Random

interface SyllableTemplate{
    val nucleusPhonemeTypes: Set<PhonemeType>
    val initialPhonemeTypes: Set<PhonemeType>
    val finalPhonemeTypes: Set<PhonemeType>

    fun test(phonemes: PhonemeSequence): Boolean

    fun createWord(phonemes: PhonemeSequence, syntaxCore: SyntaxCore): Word?
}