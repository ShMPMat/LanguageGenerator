package shmp.language.category.realization

import shmp.language.syntax.WordSequence
import shmp.language.CategoryRealization
import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.morphem.Affix


class AffixCategoryApplicator(val affix: Affix, type: CategoryRealization) :
    AbstractCategoryApplicator(type) { //TODO no guaranty for correctness
    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence =
        WordSequence(
            wordSequence.words.subList(0, wordPosition)
                    + listOf(affix.change(wordSequence[wordPosition]).copyAndAddValues(values))
                    + wordSequence.words.subList(wordPosition + 1, wordSequence.size)
        )

    override fun toString(): String {
        return affix.toString()
    }
}
