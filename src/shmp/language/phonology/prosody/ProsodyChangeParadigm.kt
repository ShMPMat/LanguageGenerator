package shmp.language.phonology.prosody

import shmp.language.Word

class ProsodyChangeParadigm(val stress: StressType) {
    fun apply(sourceWord: Word, newWord: Word): Word {
        return when (stress) {
            StressType.None -> newWord
            StressType.NotFixed -> newWord
            else -> {
                val cleanWord = newWord.copy(syllables = newWord.syllables.map { it.copy(prosodicEnums = listOf()) })
                putStressOn(cleanWord, getFixedStressPosition(stress, cleanWord))
            }
        }
    }
}