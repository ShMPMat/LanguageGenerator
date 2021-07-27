package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.WordSequence


class ConsecutiveApplicator(val applicators: List<CategoryApplicator>) : CategoryApplicator {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): WordSequence {
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
