package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.realization.CategoryRealization.SuffixWord
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType


class SuffixWordCategoryApplicator(word: Word, latch: LatchType) : WordCategoryApplicator(word, latch, SuffixWord) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        FoldedWordSequence(words.words + (word.copyWithValues(values) to latch))

    override fun copy() = SuffixWordCategoryApplicator(word, latch)

    override fun toString() = "$word, placed after the word $latch"
}
