package shmp.lang.language.category.realization

import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.CategoryRealization
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.morphem.Affix


class AffixCategoryApplicator(val affix: Affix, type: CategoryRealization) :
    AbstractCategoryApplicator(type) { //TODO no guaranty for correctness
    override fun apply(
        wordSequence: WordSequence,
        wordPosition: Int,
        values: Collection<SourcedCategoryValue>
    ) = WordSequence(
        wordSequence.words.mapIndexed { i, w ->
            if (i == wordPosition)
                affix.change(wordSequence[wordPosition]).copyAndAddValues(values)
            else w
        }
    )

    override fun toString(): String {
        return affix.toString()
    }
}
