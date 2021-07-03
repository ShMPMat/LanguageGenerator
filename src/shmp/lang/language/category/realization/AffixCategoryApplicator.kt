package shmp.lang.language.category.realization

import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.CategoryRealization
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.morphem.Affix


class AffixCategoryApplicator(val affix: Affix, type: CategoryRealization) : AbstractCategoryApplicator(type) {
    //TODO no guaranty for correctness
    override fun apply(
        words: WordSequence,
        wordPosition: Int,
        values: Collection<SourcedCategoryValue>
    ) = WordSequence(
        words.words.mapIndexed { i, w ->
            if (i == wordPosition)
                affix.change(words[wordPosition]).copyAndAddValues(values)
            else w
        }
    )

    override fun copy() = AffixCategoryApplicator(affix, type)

    override fun toString() = affix.toString()
}
