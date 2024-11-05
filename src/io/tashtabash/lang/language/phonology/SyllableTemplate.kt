package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.diachronicity.getChangingPhonemes
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

    fun applyOrNull(word: Word): Word? {
        val newSyllables = splitOnSyllables(word.toPhonemes())
            ?: return null
        var exactPhonemes = getChangingPhonemes(word, false, false)
            .map { it as ChangingPhoneme.ExactPhoneme }
        val prosodySyllables = newSyllables.map { syllable ->
            val prosody = exactPhonemes.take(syllable.size)
                .mapNotNull { it.prosody }
                .flatten()
            exactPhonemes = exactPhonemes.drop(syllable.size)

            syllable.copy(prosody = prosody)
        }

        return word.copy(syllables = prosodySyllables, syllableTemplate = this)
    }

    fun apply(word: Word): Word =
        applyOrNull(word)
            ?: throw LanguageException("Cannot apply the syllableTemplate $this to the word '$word'")
}
