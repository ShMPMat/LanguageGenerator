package io.tashtabash.lang.language.phonology.prosody

import io.tashtabash.lang.language.lexis.Word


data class ProsodyChangeParadigm(val stress: StressType) {
    fun apply(sourceWord: Word, newWord: Word): Word {
        return when (stress) {
            StressType.None -> newWord
            StressType.NotFixed -> newWord
            else -> {
                val newSyllables = newWord.syllables.map { s ->
                    s.copy(prosodicEnums = s.prosodicEnums.filter { it !is Stress })
                }
                val cleanWord = newWord.copy(syllables = newSyllables)

                putStressOn(cleanWord, getFixedStressPosition(stress, cleanWord))
            }
        }
    }
}
