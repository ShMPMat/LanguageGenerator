package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.sequence.unfold


data class WordClauseResult(val words: FoldedWordSequence, val mainWordIndex: Int) {
    fun unfold(): WordSequence =
        words.unfold()
}
