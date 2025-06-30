package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.generator.ApplicatorMap
import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.LatchedWord
import io.tashtabash.lang.language.syntax.sequence.toFoldedWordSequence


data class SpeechPartChangeParadigm(
    val speechPart: TypedSpeechPart,
    val exponenceClusters: List<ExponenceCluster> = listOf(),
    val applicators: ApplicatorMap = mapOf(),
    val prosodyChangeParadigm: ProsodyChangeParadigm = ProsodyChangeParadigm(StressType.None)
) {
    val categories = exponenceClusters.flatMap { it.categories }

    val orderedApplicators = exponenceClusters
        .map { it to applicators.getValue(it) }

    fun getCluster(cluster: ExponenceCluster) = exponenceClusters
        .firstOrNull { it == cluster }

    fun getCategoryOrNull(name: String) = categories
        .firstOrNull { it.category.outType == name }

    fun getCategory(name: String) = categories
        .first { it.category.outType == name }

    fun getCategoryValues(name: String) = getCategoryOrNull(name)
        ?.category
        ?.actualValues
        ?: emptyList()

    fun anyApplicator(predicate: (CategoryApplicator) -> Boolean): Boolean =
        applicators.values
            .flatMap { it.values }
            .any(predicate)

    fun hasPrefixes(): Boolean =
        anyApplicator { it is AffixCategoryApplicator && it.type == CategoryRealization.Prefix }

    fun hasSuffixes(): Boolean =
        anyApplicator { it is AffixCategoryApplicator && it.type == CategoryRealization.Suffix }

    fun apply(word: Word, latchType: LatchType, categoryValues: Set<SourcedCategoryValue>): WordClauseResult {
        if (word.semanticsCore.speechPart != speechPart)
            throw ChangeException("SpeechPartChangeParadigm for $speechPart received ${word.semanticsCore.speechPart}")

        var wordClauseResult = WordClauseResult(FoldedWordSequence(LatchedWord(word, latchType)), 0)
        for (exponenceCluster in exponenceClusters)
            wordClauseResult = useExponenceCluster(wordClauseResult, categoryValues, exponenceCluster)

        val currentClause = wordClauseResult.words.swapWord(wordClauseResult.mainWordIdx) {
            it.copy(syntaxRole = word.syntaxRole)
        }

        return wordClauseResult.copy(
            words = applyProsodyParadigm(currentClause, wordClauseResult.mainWordIdx, word)
        )
    }

    private fun useExponenceCluster(
        wordClauseResult: WordClauseResult,
        categoryValues: Set<SourcedCategoryValue>,
        exponenceCluster: ExponenceCluster
    ): WordClauseResult {
        val staticCategoryValues = wordClauseResult.mainWord.semanticsCore.staticCategories.mapNotNull { v ->
            val parent = exponenceCluster.categories
                .firstOrNull { it.category.outType == v.parentClassName }
                ?: return@mapNotNull null

            parent[v]
        }
        val allCategoryValues = categoryValues + staticCategoryValues
        val exponenceUnion = getExponenceUnion(allCategoryValues, exponenceCluster)
            ?: if (exponenceCluster.categories.any { c -> c.compulsoryData.mustExist(allCategoryValues.map { it.categoryValue }) })
                throw SyntaxException("No value for compulsory cluster $exponenceCluster")
            else
                return wordClauseResult
        val actualValues = allCategoryValues.filter { it in exponenceUnion.categoryValues }
        val newClause = useCategoryApplicator(
            wordClauseResult.words,
            wordClauseResult.mainWordIdx,
            exponenceCluster,
            exponenceUnion,
            actualValues
        )

        var newWordPosition = wordClauseResult.mainWordIdx
        if (wordClauseResult.words.size != newClause.size)
            for (i in wordClauseResult.mainWordIdx until newClause.size)
                if (wordClauseResult.mainWord == newClause[i].word) {
                    newWordPosition = i
                    break
                }

        return WordClauseResult(newClause, newWordPosition)
    }

    private fun useCategoryApplicator(
        wordSequence: FoldedWordSequence,
        wordPosition: Int,
        exponenceCluster: ExponenceCluster,
        exponenceValue: ExponenceValue,
        actualValues: List<SourcedCategoryValue>
    ) = if (applicators[exponenceCluster]?.containsKey(exponenceValue) == true)
        applicators[exponenceCluster]
            ?.get(exponenceValue)
            ?.apply(wordSequence, wordPosition, actualValues)
            ?: throw ChangeException(
                "Tried to change word \"${wordSequence[wordPosition].word}\" for categories $exponenceValue " +
                        "but such Exponence Cluster isn't defined"
            )
    else wordSequence.words.map { (w, l) -> LatchedWord(w.copy(), l) }.toFoldedWordSequence()

    private fun getExponenceUnion(
        categoryValues: Set<SourcedCategoryValue>,
        exponenceCluster: ExponenceCluster
    ) = exponenceCluster.filterExponenceUnion(categoryValues)

    private fun applyProsodyParadigm(wordSequence: FoldedWordSequence, wordPosition: Int, oldWord: Word): FoldedWordSequence {
        val (word, latch) = wordSequence[wordPosition]

        return FoldedWordSequence(
            wordSequence.words.subList(0, wordPosition)
                    + LatchedWord(prosodyChangeParadigm.apply(oldWord, word), latch)
                    + wordSequence.words.subList(wordPosition + 1, wordSequence.words.size)
        )
    }

    fun hasChanges() = applicators.any { it.value.isNotEmpty() }

    override fun toString() = "$speechPart changes on: \n" +
            exponenceClusters.map { it to applicators.getValue(it) }
                .joinToString("\n\n") { (c, m) ->
                    "$c:\n" + m.entries
                        .map { it.key.toString() + ": " + it.value }
                        .sortedWith(naturalOrder())
                        .joinToString("\n")
                }
}
