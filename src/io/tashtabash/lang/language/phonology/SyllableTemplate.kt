package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.lexis.SemanticsCore
import io.tashtabash.lang.language.lexis.Word


interface SyllableTemplate {
    val nucleusPhonemeTypes: Set<PhonemeType>
    val initialPhonemeTypes: Set<PhonemeType>
    val finalPhonemeTypes: Set<PhonemeType>

    fun test(phonemes: PhonemeSequence): Boolean

    fun splitOnSyllables(phonemes: PhonemeSequence): Syllables?

    fun createWord(phonemes: PhonemeSequence, semanticsCore: SemanticsCore): Word? =
        splitOnSyllables(phonemes)?.let { syllables ->
            Word(syllables, this, semanticsCore)
        }
}
