package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.generator.ApplicatorMap
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.toFoldedWordSequence


data class SpeechPartChangeParadigm(
    val speechPart: TypedSpeechPart,
    val exponenceClusters: List<ExponenceCluster>,
    val applicators: ApplicatorMap,
    val prosodyChangeParadigm: ProsodyChangeParadigm
) {
    val categories = exponenceClusters.flatMap { it.categories }

    val orderedApplicators = exponenceClusters
        .map { it to applicators.getValue(it) }

    fun getCategory(name: String) = categories
        .first { it.category.outType == name }

    fun getCluster(cluster: ExponenceCluster) = exponenceClusters.firstOrNull { it == cluster }

    fun getCategoryOrNull(name: String) = categories
        .firstOrNull { it.category.outType == name }

    fun getCategoryValues(name: String) = getCategoryOrNull(name)
        ?.category
        ?.actualValues
        ?: emptyList()

    fun apply(word: Word, latchType: LatchType, categoryValues: Set<SourcedCategoryValue>): Pair<FoldedWordSequence, Int> {
        if (word.semanticsCore.speechPart != speechPart) throw ChangeException(
            "SpeechPartChangeParadigm for $speechPart has been given ${word.semanticsCore.speechPart}"
        )

        var currentClause = FoldedWordSequence(word to latchType)
        var currentWord = word
        var wordPosition = 0
        for (exponenceCluster in exponenceClusters) {
            if (word.categoryValues.map { it.parent }.containsAll(exponenceCluster.categories))
                continue

            val staticCategoryValues = word.semanticsCore.staticCategories.mapNotNull { v ->
                val parent = exponenceCluster.categories
                    .firstOrNull { it.category.outType == v.parentClassName }
                    ?: return@mapNotNull null

                SourcedCategoryValue(v, CategorySource.Self, parent)
            }
            val allCategoryValues = categoryValues + staticCategoryValues
            val exponenceUnion = getExponenceUnion(allCategoryValues, exponenceCluster)
                ?: if (exponenceCluster.categories.any { c -> c.compulsoryData.mustExist(allCategoryValues.map { it.categoryValue }) })
                    throw SyntaxException("No value for compulsory cluster $exponenceCluster")
                else continue
            val actualValues = allCategoryValues.filter { it in exponenceUnion.categoryValues }
            val newClause = useCategoryApplicator(
                currentClause,
                wordPosition,
                exponenceCluster,
                exponenceUnion,
                actualValues
            )
            if (currentClause.size != newClause.size)
                for (i in wordPosition until newClause.size)
                    if (currentWord == newClause[i].first) {
                        wordPosition = i
                        break
                    }
            currentWord = newClause[wordPosition].first
            currentClause = newClause
        }

        currentClause = currentClause.swapWord(wordPosition) { it.copy(syntaxRole = word.syntaxRole) }

        return applyProsodyParadigm(currentClause, wordPosition, word) to wordPosition
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
                "Tried to change word \"${wordSequence[wordPosition].first}\" for categories $exponenceValue " +
                        "but such Exponence Cluster isn't defined"
            )
    else wordSequence.words.map { (w, l) -> w.copy() to l }.toFoldedWordSequence()

    private fun getExponenceUnion(
        categoryValues: Set<SourcedCategoryValue>,
        exponenceCluster: ExponenceCluster
    ) = exponenceCluster.filterExponenceUnion(categoryValues)

    private fun applyProsodyParadigm(wordSequence: FoldedWordSequence, wordPosition: Int, oldWord: Word): FoldedWordSequence {
        val (word, latch) = wordSequence[wordPosition]

        return FoldedWordSequence(
            wordSequence.words.subList(0, wordPosition)
                    + (prosodyChangeParadigm.apply(oldWord, word) to latch)
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
