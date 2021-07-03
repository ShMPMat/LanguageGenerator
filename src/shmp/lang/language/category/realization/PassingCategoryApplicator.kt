package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.WordSequence


object PassingCategoryApplicator : CategoryApplicator {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): WordSequence =
        WordSequence(
            words.words.mapIndexed { i, w ->
                if (i == wordPosition)
                    w.copyAndAddValues(values)
                else w
            }
        )

    override fun copy() = PassingCategoryApplicator

    override fun toString() = "Nothing"
}
