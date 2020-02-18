package shmp.language.nominal_categories.change

import shmp.language.Clause

interface CategoryApplicator {
    fun apply(clause: Clause, wordPosition: Int): Clause
}