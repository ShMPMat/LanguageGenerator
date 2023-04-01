package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence


class FilterApplicator(val applicators: List<Pair<CategoryApplicator, List<Word>>>) : AbstractCategoryApplicator(null) {
    constructor(vararg applicators: Pair<CategoryApplicator, List<Word>>) : this(applicators.toList())

    init {
        if (applicators.isEmpty())
            throw LanguageException("Applicators cannot be empty")

        if (applicators.last().second.isNotEmpty())
            throw LanguageException("Last applicator word list must be empty")
    }

    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): FoldedWordSequence {
        val (currentWord) = words[wordPosition]

        for ((i, applicatorToWords) in applicators.dropLast(1).withIndex()) {
            val (applicator, acceptableWords) = applicatorToWords

            if (currentWord !in acceptableWords)
                continue

            return applicator.apply(words, wordPosition, values)
        }

        return applicators.last().first.apply(words, wordPosition, values)
    }

    override fun copy() = FilterApplicator(applicators)

    override fun toString() = applicators
        .mapIndexed { i, (a, ws) ->
            val prefix = if (i != applicators.size)
                "if word in ${ws.joinToString()} "
            else ""

            prefix + a
        }.joinToString(", otherwise ")
}
