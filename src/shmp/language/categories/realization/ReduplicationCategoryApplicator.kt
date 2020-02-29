package shmp.language.categories.realization

import shmp.language.Clause

class ReduplicationCategoryApplicator : CategoryApplicator {
    override fun apply(clause: Clause, wordPosition: Int) = Clause(
        clause.words.take(wordPosition)
                + clause[wordPosition]
                + clause[wordPosition].copy()
                + clause.words.drop(wordPosition + 1)
    )

    override fun toString(): String {
        return "Reduplication"
    }
}