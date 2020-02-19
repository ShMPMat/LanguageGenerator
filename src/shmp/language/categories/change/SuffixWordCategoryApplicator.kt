package shmp.language.categories.change

import shmp.language.Clause
import shmp.language.NominalCategoryRealization
import shmp.language.Word

class SuffixWordCategoryApplicator(suffixWord: Word) :
    WordCategoryApplicator(suffixWord, NominalCategoryRealization.SuffixSeparateWord) {
    override fun apply(clause: Clause, wordPosition: Int): Clause = Clause(clause.words + listOf(applicatorWord))

    override fun toString(): String {
        return "$applicatorWord, placed after the word"
    }
}