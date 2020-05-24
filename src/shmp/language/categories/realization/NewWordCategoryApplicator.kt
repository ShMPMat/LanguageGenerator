package shmp.language.categories.realization

import shmp.language.CategoryRealization
import shmp.language.Clause
import shmp.language.Word

class NewWordCategoryApplicator(applicatorWord: Word) :
    WordCategoryApplicator(applicatorWord, CategoryRealization.NewWord) {

    override fun apply(clause: Clause, wordPosition: Int) = Clause(
        clause.words.mapIndexed { i, w -> if (i == wordPosition) applicatorWord.copy() else w }
    )

    override fun toString() = "New word: $applicatorWord"
}