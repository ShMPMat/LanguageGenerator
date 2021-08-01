package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.sequence.FoldedWordSequence


class ConsecutiveApplicator(val applicators: List<CategoryApplicator>) : AbstractCategoryApplicator(null) {
    constructor(vararg applicators: CategoryApplicator) : this(applicators.toList())

    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): FoldedWordSequence {
        var currentClause = words
        var currentWord = words[wordPosition]
        var i = wordPosition

        for (applicator in applicators) {
            val newClause = applicator.apply(currentClause, i, values)

            if (currentClause.size != newClause.size)
                for (j in i until newClause.size)
                    if (currentWord == newClause[i]) {
                        i = j
                        break
                    }
            currentWord = newClause[i]
            currentClause = newClause.swapWord(i) { it.copyWithValues(it.categoryValues subtract values) }
        }

        return currentClause.swapWord(i) { it.copyAndAddValues(values) }
    }

    override fun copy() = ConsecutiveApplicator(applicators)

    override fun toString() = applicators.joinToString(", then ")
}
