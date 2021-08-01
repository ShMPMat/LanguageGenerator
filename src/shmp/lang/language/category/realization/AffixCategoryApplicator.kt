package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.morphem.Affix
import shmp.lang.language.syntax.sequence.FoldedWordSequence


class AffixCategoryApplicator(val affix: Affix, type: CategoryRealization?) : AbstractCategoryApplicator(type) {
    //TODO no guaranty for correctness
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { affix.change(it).copyAndAddValues(values) }

    override fun copy() = AffixCategoryApplicator(affix, type)

    override fun toString() = affix.toString()
}
