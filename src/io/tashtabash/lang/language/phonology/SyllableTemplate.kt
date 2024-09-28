package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.lexis.SemanticsCore
import io.tashtabash.lang.language.lexis.Word


interface SyllableTemplate {
    val nucleusPhonemeTypes: Set<PhonemeType>
    val initialPhonemeTypes: Set<PhonemeType>
    val finalPhonemeTypes: Set<PhonemeType>

    val maxSize: Int

    fun splitOnSyllables(phonemes: PhonemeSequence): Syllables?
    fun addInitial(initial: PhonemeType): SyllableTemplate
    fun addFinal(initial: PhonemeType): SyllableTemplate
    fun merge(that: SyllableTemplate): SyllableTemplate

    fun splitOnSyllables(phonemes: List<Phoneme>) =
        splitOnSyllables(PhonemeSequence(phonemes))

    fun createWord(phonemes: PhonemeSequence, semanticsCore: SemanticsCore): Word? =
        splitOnSyllables(phonemes)?.let { syllables ->
            Word(syllables, this, semanticsCore)
        }

    fun apply(word: Word): Word? {
        val fixedSyllables = splitOnSyllables(word.toPhonemes())
            ?: return null

        return word.copy(syllables = fixedSyllables, syllableTemplate = this)
    }
}
