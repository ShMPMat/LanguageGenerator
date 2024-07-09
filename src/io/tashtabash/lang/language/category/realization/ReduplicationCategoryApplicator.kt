package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchedWord


class ReduplicationCategoryApplicator : AbstractCategoryApplicator(CategoryRealization.Reduplication) {
    override fun apply(
        words: FoldedWordSequence,
        wordPosition: Int,
        values: Collection<SourcedCategoryValue>
    ): FoldedWordSequence {
        val newWord = words[wordPosition].word.copyAndAddValues(values)
        val latchType = words[wordPosition].latchType

        return FoldedWordSequence(
            words.words.take(wordPosition)
                    + (LatchedWord(newWord, latchType))
                    + (LatchedWord(newWord.copy(), latchType))
                    + words.words.drop(wordPosition + 1)
        )
    }

    override fun copy() = ReduplicationCategoryApplicator()

    override fun toString() = "Reduplication"
}
