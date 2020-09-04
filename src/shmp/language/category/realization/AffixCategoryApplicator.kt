package shmp.language.category.realization

import shmp.language.syntax.Clause
import shmp.language.CategoryRealization
import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.morphem.Affix


class AffixCategoryApplicator(val affix: Affix, type: CategoryRealization) :
    AbstractCategoryApplicator(type) { //TODO no guaranty for correctness
    override fun apply(clause: Clause, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): Clause =
        Clause(
            clause.words.subList(0, wordPosition)
                    + listOf(affix.change(clause[wordPosition]).copyAndAddValues(values))
                    + clause.words.subList(wordPosition + 1, clause.size)
        )

    override fun toString(): String {
        return affix.toString()
    }
}
