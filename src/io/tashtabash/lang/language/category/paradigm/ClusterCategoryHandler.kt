package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.syntax.sequence.LatchedWord
import io.tashtabash.lang.language.syntax.sequence.toFoldedWordSequence


// Responsible for applying synthetic & analytic categories
interface CategoryHandler {
    fun apply(
        wordClause: WordClauseResult,
        exponenceValue: ExponenceValue,
        actualValues: List<SourcedCategoryValue>
    ): WordClauseResult

    fun copy(): CategoryHandler
}

data class SyntheticCategoryHandler(val applicatorSource: ApplicatorSource): CategoryHandler {
    override fun apply(
        wordClause: WordClauseResult,
        exponenceValue: ExponenceValue,
        actualValues: List<SourcedCategoryValue>
    ): WordClauseResult {
        // No main word to change, a construction has been applied
        if (wordClause.mainWord == null || wordClause.mainWordIdx == null)
            return wordClause

        val newClause = if (applicatorSource.map.containsKey(exponenceValue))
            applicatorSource.map[exponenceValue]
                ?.apply(wordClause.words, wordClause.mainWordIdx, actualValues)
                ?: throw ChangeException(
                    "Tried to change word \"${wordClause.mainWord}\" for categories $exponenceValue " +
                            "but such Exponence Cluster isn't defined"
                )
        else wordClause.words.words.map { (w, l) -> LatchedWord(w.copy(), l) }.toFoldedWordSequence()

        var newWordPosition = wordClause.mainWordIdx
        if (wordClause.words.size != newClause.size)
            for (i in wordClause.mainWordIdx until newClause.size)
                if (wordClause.mainWord == newClause[i].word) {
                    newWordPosition = i
                    break
                }

        return WordClauseResult(newClause, newWordPosition)
    }

    override fun copy() = SyntheticCategoryHandler(applicatorSource.copy())

    override fun toString() = "$applicatorSource\n" + applicatorSource.map.entries
        .map { it.key.toString() + ": " + it.value }
        .sortedWith(naturalOrder())
        .joinToString("\n")
}

data class LinkCategoryHandler(val source: SpeechPartChangeParadigm, val applicatorIdx: Int): CategoryHandler {
    val target: CategoryHandler = source.applicators[applicatorIdx].second.let {
        if (it is LinkCategoryHandler)
            it.target
        else
            it
    }

    override fun apply(
        wordClause: WordClauseResult,
        exponenceValue: ExponenceValue,
        actualValues: List<SourcedCategoryValue>
    ): WordClauseResult = source.applicators[applicatorIdx]
        .second
        .apply(wordClause, exponenceValue, actualValues)

    override fun copy() = LinkCategoryHandler(source, applicatorIdx)

    override fun toString() =
        " (The same as for ${source.speechPart})"
}
