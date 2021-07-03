package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.WordSequence


interface CategoryApplicator {
    fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): WordSequence

    fun copy(): CategoryApplicator
}
