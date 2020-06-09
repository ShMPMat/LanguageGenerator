package shmp.language.category.realization

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.syntax.Clause

interface CategoryApplicator {
    fun apply(clause: Clause, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): Clause
}