package shmp.language.categories.realization

import shmp.language.Clause

class PassingCategoryApplicator : CategoryApplicator {
    override fun apply(clause: Clause, wordPosition: Int) = Clause(
        clause.words.map { it }
    )

    override fun toString() = "Nothing"
}