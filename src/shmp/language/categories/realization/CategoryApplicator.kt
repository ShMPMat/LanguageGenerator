package shmp.language.categories.realization

import shmp.language.Clause

interface CategoryApplicator {
    fun apply(clause: Clause, wordPosition: Int): Clause
}