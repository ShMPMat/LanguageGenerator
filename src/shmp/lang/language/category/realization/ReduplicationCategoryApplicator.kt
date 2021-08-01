package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.sequence.FoldedWordSequence


class ReduplicationCategoryApplicator : AbstractCategoryApplicator(CategoryRealization.Reduplication) {
    override fun apply(
        words: FoldedWordSequence,
        wordPosition: Int,
        values: Collection<SourcedCategoryValue>
    ): FoldedWordSequence {
        val newWord = words[wordPosition].first.copyAndAddValues(values)
        val latchType = words[wordPosition].second

        return FoldedWordSequence(
            words.words.take(wordPosition)
                    + (newWord to latchType)
                    + (newWord.copy() to latchType)
                    + words.words.drop(wordPosition + 1)
        )
    }

    override fun copy() = ReduplicationCategoryApplicator()

    override fun toString() = "Reduplication"
}
