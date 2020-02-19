package shmp.language.categories.change

import shmp.language.Clause

interface CategoryApplicator {
    fun apply(clause: Clause, wordPosition: Int): Clause
}