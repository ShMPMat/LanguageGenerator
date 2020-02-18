package shmp.language.nominal_categories.change

import shmp.language.Clause
import shmp.language.NominalCategoryRealization
import shmp.language.Word
import shmp.language.morphem.Affix

class AffixCategoryApplicator(val affix: Affix, type: NominalCategoryRealization) :
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