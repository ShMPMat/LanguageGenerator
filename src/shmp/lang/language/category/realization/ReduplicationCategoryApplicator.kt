package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.ParametrizedCategoryValue
import shmp.lang.language.syntax.WordSequence

class ReduplicationCategoryApplicator : CategoryApplicator {
    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence {
        val newWord = wordSequence[wordPosition].copyAndAddValues(values)
        return WordSequence(
            wordSequence.words.take(wordPosition)
                    + newWord
                    + newWord.copy()
                    + wordSequence.words.drop(wordPosition + 1)
        )
    }

    override fun toString() = "Reduplication"
}