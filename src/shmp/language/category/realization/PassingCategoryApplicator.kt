package shmp.language.category.realization

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.syntax.Clause

class PassingCategoryApplicator : CategoryApplicator {
    override fun apply(clause: Clause, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): Clause =
        Clause(
            clause.words.mapIndexed { i, w ->
                if (i == wordPosition)
                    w.copyAndAddValues(values)
                else w
            }
        )

    override fun toString() = "Nothing"
}