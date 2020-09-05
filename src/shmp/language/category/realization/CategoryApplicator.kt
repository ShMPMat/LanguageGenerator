package shmp.language.category.realization

import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.syntax.WordSequence

interface CategoryApplicator {
    fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence
}