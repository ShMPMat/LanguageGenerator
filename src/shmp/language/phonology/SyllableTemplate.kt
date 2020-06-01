package shmp.language.phonology

import shmp.language.*

interface SyllableTemplate{
    val nucleusPhonemeTypes: Set<PhonemeType>
    val initialPhonemeTypes: Set<PhonemeType>
    val finalPhonemeTypes: Set<PhonemeType>

    fun test(phonemes: PhonemeSequence): Boolean

    fun createWord(phonemes: PhonemeSequence, semanticsCore: SemanticsCore): Word?
}