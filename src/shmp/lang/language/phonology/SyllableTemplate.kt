package shmp.lang.language.phonology

import shmp.lang.language.PhonemeType
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.Word


interface SyllableTemplate {
    val nucleusPhonemeTypes: Set<PhonemeType>
    val initialPhonemeTypes: Set<PhonemeType>
    val finalPhonemeTypes: Set<PhonemeType>

    fun test(phonemes: PhonemeSequence): Boolean

    fun splitOnSyllables(phonemes: PhonemeSequence): List<Syllable>?

    fun createWord(phonemes: PhonemeSequence, semanticsCore: SemanticsCore): Word? =
        splitOnSyllables(phonemes)?.let { syllables ->
            Word(syllables, this, semanticsCore)
        }
}
