package shmp.language.category.realization

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.syntax.Clause

class ReduplicationCategoryApplicator : CategoryApplicator {
    override fun apply(clause: Clause, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): Clause {
        val newWord = clause[wordPosition].copyAndAddValues(values)
        return Clause(
            clause.words.take(wordPosition)
                    + newWord
                    + newWord.copy()
                    + clause.words.drop(wordPosition + 1)
        )
    }

    override fun toString() = "Reduplication"
}