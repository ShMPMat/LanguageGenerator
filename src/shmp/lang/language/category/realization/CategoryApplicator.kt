package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.ParametrizedCategoryValue
import shmp.lang.language.syntax.WordSequence

interface CategoryApplicator {
    fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence
}