package shmp.language.category.realization

import shmp.language.CategoryRealization
import shmp.language.syntax.Clause
import shmp.language.lexis.Word
import shmp.language.category.paradigm.ParametrizedCategoryValue

class NewWordCategoryApplicator(applicatorWord: Word) :
    WordCategoryApplicator(applicatorWord, CategoryRealization.NewWord) {

    override fun apply(clause: Clause, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): Clause =
        Clause(
            clause.words.mapIndexed { i, w ->
                if (i == wordPosition)
                    applicatorWord.copyWithValues(values)
                else w
            }
        )

    override fun toString() = "New word: $applicatorWord"
}