package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.LatchedWord


data class SpeechPartChangeParadigm(
    val speechPart: TypedSpeechPart,
    val applicators: List<Pair<ExponenceCluster, CategoryHandler>> = listOf(),
    val prosodyChangeParadigm: ProsodyChangeParadigm = ProsodyChangeParadigm(StressType.None)
) {
    val sources by lazy {
        applicators.map { (_, map) -> map }
    }

    val categories by lazy {
        applicators.flatMap { it.first.categories }
    }

    fun getCluster(cluster: ExponenceCluster): ExponenceCluster? = applicators
        .firstOrNull { it.first == cluster }
        ?.first

    operator fun get(name: String): SourcedCategory? = categories
        .firstOrNull { it.category.outType == name }

    fun getValue(name: String) = categories
        .first { it.category.outType == name }

    fun getValueOrEmpty(categoryName: String, value: CategoryValue): List<SourcedCategoryValue> =
        get(categoryName)
            ?.getOrNull(value)
            ?.let { listOf(it) }
            ?: listOf()

    fun getCategoryValues(name: String) = get(name)
        ?.category
        ?.actualValues
        ?: emptyList()

    private fun anyApplicator(predicate: (CategoryApplicator) -> Boolean): Boolean =
        applicators.map { it.second }
            .filterIsInstance<SyntheticCategoryHandler>()
            .flatMap { it.applicatorSource.map.values }
            .any(predicate)

    fun hasPrefixes(): Boolean =
        anyApplicator { it is AffixCategoryApplicator && it.type == CategoryRealization.Prefix }

    fun hasSuffixes(): Boolean =
        anyApplicator { it is AffixCategoryApplicator && it.type == CategoryRealization.Suffix }

    fun apply(word: Word, latchType: LatchType, categoryValues: Set<SourcedCategoryValue>): WordClauseResult {
        if (word.semanticsCore.speechPart != speechPart)
            throw ChangeException("SpeechPartChangeParadigm for $speechPart received ${word.semanticsCore.speechPart}")

        var wordClauseResult = WordClauseResult(FoldedWordSequence(LatchedWord(word, latchType)), 0)
        for ((exponenceCluster, applicator) in applicators)
            wordClauseResult = useExponenceCluster(wordClauseResult, categoryValues, exponenceCluster, applicator)

        if (wordClauseResult.mainWordIdx == null)
            return wordClauseResult

        return wordClauseResult.copy(
            words = applyProsodyParadigm(wordClauseResult.words, wordClauseResult.mainWordIdx, word)
        )
    }

    private fun useExponenceCluster(
        wordClauseResult: WordClauseResult,
        categoryValues: Set<SourcedCategoryValue>,
        exponenceCluster: ExponenceCluster,
        applicator: CategoryHandler
    ): WordClauseResult {
        // No main word to change, a construction has been applied
        if (wordClauseResult.mainWord == null || wordClauseResult.mainWordIdx == null)
            return wordClauseResult

        val staticCategoryValues = wordClauseResult.mainWord.semanticsCore.staticCategories.mapNotNull { staticValue ->
            exponenceCluster.categories
                .firstOrNull { it.category.outType == staticValue.parentClassName }
                ?.get(staticValue)
        }
        val allCategoryValues = categoryValues + staticCategoryValues
        val allUnsourcedCategoryValues = allCategoryValues.map { it.categoryValue }
        val exponenceUnion = getExponenceUnion(allCategoryValues, exponenceCluster)
            ?: if (exponenceCluster.categories.any { c -> c.compulsoryData.mustExist(allUnsourcedCategoryValues) })
                throw SyntaxException("No value for compulsory cluster $exponenceCluster")
            else
                return wordClauseResult
        val actualValues = allCategoryValues.filter { it in exponenceUnion.categoryValues }
        return applicator.apply(wordClauseResult, exponenceUnion, actualValues)
    }

    private fun getExponenceUnion(
        categoryValues: Set<SourcedCategoryValue>,
        exponenceCluster: ExponenceCluster
    ) = exponenceCluster.filterExponenceUnion(categoryValues)

    private fun applyProsodyParadigm(
        wordSequence: FoldedWordSequence,
        wordPosition: Int,
        oldWord: Word
    ): FoldedWordSequence {
        val (word, latch) = wordSequence[wordPosition]

        return FoldedWordSequence(
            wordSequence.words.subList(0, wordPosition)
                    + LatchedWord(prosodyChangeParadigm.apply(oldWord, word), latch)
                    + wordSequence.words.subList(wordPosition + 1, wordSequence.words.size)
        )
    }

    fun hasChanges() = applicators.any { (_, h) ->
        h is SyntheticCategoryHandler && h.applicatorSource.map.isNotEmpty()
    }

    private fun isLinkOnly(): Boolean =
        applicators.map { it.second }
            .filterIsInstance<LinkCategoryHandler>()
            .count() == applicators.size

    private fun listAllLinkSources(): List<TypedSpeechPart> =
        applicators.map { it.second }
            .filterIsInstance<LinkCategoryHandler>()
            .map { it.source.speechPart }
            .distinct()

    override fun toString() =
        if (isLinkOnly() && listAllLinkSources().size == 1)
            "$speechPart has the same paradigm as ${listAllLinkSources()[0]}"
        else
            "$speechPart changes on: \n" +
                applicators.joinToString("\n\n") { (c, h) ->
                    "$c:$h"
                }
}
