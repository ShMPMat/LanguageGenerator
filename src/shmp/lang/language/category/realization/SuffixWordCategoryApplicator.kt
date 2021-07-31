package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization.SuffixWord
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.sequence.FoldedWordSequence
import shmp.lang.language.syntax.sequence.LatchType


class SuffixWordCategoryApplicator(word: Word, latch: LatchType) : WordCategoryApplicator(word, latch, SuffixWord) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        FoldedWordSequence(words.words + (word.copyWithValues(values) to latch))

    override fun copy() = SuffixWordCategoryApplicator(word, latch)

    override fun toString() = "$word, placed after the word $latch"
}
