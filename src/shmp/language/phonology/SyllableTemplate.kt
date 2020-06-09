package shmp.language.phonology

import shmp.language.*
import shmp.language.lexis.SemanticsCore
import shmp.language.lexis.Word

interface SyllableTemplate{
    val nucleusPhonemeTypes: Set<PhonemeType>
    val initialPhonemeTypes: Set<PhonemeType>
    val finalPhonemeTypes: Set<PhonemeType>

    fun test(phonemes: PhonemeSequence): Boolean

    fun createWord(phonemes: PhonemeSequence, semanticsCore: SemanticsCore): Word?
}