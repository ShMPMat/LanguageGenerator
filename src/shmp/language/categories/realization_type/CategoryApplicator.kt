package shmp.language.categories.realization_type

import shmp.language.Clause

interface CategoryApplicator {
    fun apply(clause: Clause, wordPosition: Int): Clause
}