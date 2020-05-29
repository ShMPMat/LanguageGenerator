package shmp.language.category.realization

import shmp.language.Clause
import shmp.language.CategoryRealization
import shmp.language.Word

class SuffixWordCategoryApplicator(suffixWord: Word) :
    WordCategoryApplicator(suffixWord, CategoryRealization.SuffixSeparateWord) {
    override fun apply(clause: Clause, wordPosition: Int): Clause = Clause(clause.words + listOf(applicatorWord))

    override fun toString(): String {
        return "$applicatorWord, placed after the word"
    }
}