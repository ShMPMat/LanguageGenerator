package shmp.language.categories.realization_type

import shmp.language.Clause
import shmp.language.CategoryRealization
import shmp.language.morphem.Affix

class AffixCategoryApplicator(val affix: Affix, type: CategoryRealization) :
    AbstractCategoryApplicator(type) { //TODO no guaranty for correctness
    override fun apply(clause: Clause, wordPosition: Int) = Clause(
        clause.words.subList(0, wordPosition)
                + listOf(affix.change(clause[wordPosition]))
                + clause.words.subList(wordPosition + 1, clause.size)
    )

    override fun toString(): String {
        return affix.toString()
    }
}