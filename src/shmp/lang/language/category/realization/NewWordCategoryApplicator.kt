package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.paradigm.SourcedCategoryValue

class NewWordCategoryApplicator(applicatorWord: Word) :
    WordCategoryApplicator(applicatorWord, CategoryRealization.NewWord) {

    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): WordSequence =
        WordSequence(
            wordSequence.words.mapIndexed { i, w ->
                if (i == wordPosition)
                    applicatorWord.copyAndAddValues(w.categoryValues + values)
                else w
            }
        )

    override fun toString() = "New word: $applicatorWord"
}