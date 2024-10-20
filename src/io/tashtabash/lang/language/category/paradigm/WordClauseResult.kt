package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.sequence.unfold


data class WordClauseResult(val words: FoldedWordSequence, val mainWordIdx: Int) {
    val mainWord: Word = words[mainWordIdx].word

    fun unfold(): WordSequence =
        words.unfold()
}
