package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.paradigm.ParametrizedCategoryValue

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