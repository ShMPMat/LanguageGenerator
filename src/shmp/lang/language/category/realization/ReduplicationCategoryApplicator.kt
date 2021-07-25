package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.WordSequence


class ReduplicationCategoryApplicator : CategoryApplicator {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): WordSequence {
        val newWord = words[wordPosition].copyAndAddValues(values)

        return WordSequence(
            words.words.take(wordPosition)
                    + newWord
                    + newWord.copy()
                    + words.words.drop(wordPosition + 1)
        )
    }

    override fun copy() = ReduplicationCategoryApplicator()

    override fun toString() = "Reduplication"
}
