package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.sequence.unfold


// mainWordIdx is null when all words still have to be processed
data class WordClauseResult(val words: FoldedWordSequence, val mainWordIdx: Int?) {
    val mainWord: Word? = if (mainWordIdx != null) words[mainWordIdx].word else null

    fun unfold(): WordSequence =
        words.unfold()

    fun map(mapper: (Word) -> Word): WordClauseResult =
        copy(words = words.map(mapper))
}
