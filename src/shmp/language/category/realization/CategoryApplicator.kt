package shmp.language.category.realization

import shmp.language.Clause

interface CategoryApplicator {
    fun apply(clause: Clause, wordPosition: Int): Clause
}