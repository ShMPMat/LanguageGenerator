package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.ParametrizedCategoryValue
import shmp.lang.language.syntax.WordSequence

class PassingCategoryApplicator : CategoryApplicator {
    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence =
        WordSequence(
            wordSequence.words.mapIndexed { i, w ->
                if (i == wordPosition)
                    w.copyAndAddValues(values)
                else w
            }
        )

    override fun toString() = "Nothing"
}