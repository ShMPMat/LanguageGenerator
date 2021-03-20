package shmp.lang.language.phonology.prosody

import shmp.lang.language.lexis.Word

class ProsodyChangeParadigm(val stress: StressType) {
    fun apply(sourceWord: Word, newWord: Word): Word {
        return when (stress) {
            StressType.None -> newWord
            StressType.NotFixed -> newWord
            else -> {
                val cleanWord = newWord.copy(syllables = newWord.syllables.map { s ->
                    s.copy(prosodicEnums = s.prosodicEnums.filter { it !is Stress })
                })
                putStressOn(cleanWord, getFixedStressPosition(stress, cleanWord))
            }
        }
    }
}