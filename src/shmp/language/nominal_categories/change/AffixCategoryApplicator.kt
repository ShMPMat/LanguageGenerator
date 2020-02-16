package shmp.language.nominal_categories.change

import shmp.language.Clause
import shmp.language.NominalCategoryRealization
import shmp.language.Word
import shmp.language.morphem.Affix

class AffixCategoryApplicator(val affix: Affix, type: NominalCategoryRealization) : AbstractCategoryApplicator(type) { //TODO no guaranty for correctness
    override fun apply(word: Word): Clause = Clause(listOf(affix.change(word)))

    override fun toString(): String {
        return affix.toString()
    }
}