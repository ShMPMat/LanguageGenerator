package shmp.language.category.realization

import shmp.language.CategoryRealization
import shmp.language.syntax.WordSequence
import shmp.language.lexis.Word
import shmp.language.category.paradigm.ParametrizedCategoryValue

class NewWordCategoryApplicator(applicatorWord: Word) :
    WordCategoryApplicator(applicatorWord, CategoryRealization.NewWord) {

    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence =
        WordSequence(
            wordSequence.words.mapIndexed { i, w ->
                if (i == wordPosition)
                    applicatorWord.copyWithValues(values)
                else w
            }
        )

    override fun toString() = "New word: $applicatorWord"
}