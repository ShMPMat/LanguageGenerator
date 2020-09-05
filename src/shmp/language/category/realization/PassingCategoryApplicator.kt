package shmp.language.category.realization

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.syntax.WordSequence

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