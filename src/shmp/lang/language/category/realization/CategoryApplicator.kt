package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.FoldedWordSequence
import shmp.lang.language.syntax.WordSequence


interface CategoryApplicator {
    fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): FoldedWordSequence

    fun copy(): CategoryApplicator
}
