package shmp.lang.language.category.paradigm

import shmp.lang.language.category.Category
import shmp.lang.language.category.Deixis
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.realization.CategoryApplicator
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.phonology.prosody.ProsodyChangeParadigm
import shmp.lang.language.syntax.WordSequence

class SpeechPartChangeParadigm(
    val speechPart: TypedSpeechPart,
    val exponenceClusters: List<ExponenceCluster>,
    val applicators: Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>>,
    val prosodyChangeParadigm: ProsodyChangeParadigm
) {
    val categories = exponenceClusters.flatMap { it.categories }

    inline fun <reified E: Category> getCategory() = categories
        .first { it.category is E }

    inline fun <reified E: Category> getCategoryValues() = categories
        .firstOrNull { it.category is E }
        ?.category
        ?.actualValues
        ?: emptyList()

    fun apply(word: Word, categoryValues: Set<SourcedCategoryValue>): Pair<WordSequence, Int> {
        if (word.semanticsCore.speechPart != speechPart) throw ChangeException(
            "SpeechPartChangeParadigm for $speechPart has been given ${word.semanticsCore.speechPart}"
        )

        var currentClause = WordSequence(listOf(word))
        var currentWord = word
        var wordPosition = 0
        for (exponenceCluster in exponenceClusters) {
            val exponenceUnion = getExponenceUnion(categoryValues, exponenceCluster) ?: continue
            val actualValues = categoryValues.filter { it in exponenceUnion.categoryValues }
            val newClause = useCategoryApplicator(
                currentClause,
                wordPosition,
                exponenceCluster,
                exponenceUnion,
                actualValues
            )
            if (currentClause.size != newClause.size) {
                for (i in wordPosition until newClause.size) {
                    if (currentWord == newClause[i]) {
                        wordPosition = i
                        break
                    }
                }
            } else
                currentWord = newClause[wordPosition]
            currentClause = newClause
        }

        currentClause = WordSequence(
            currentClause.words.mapIndexed { i, w ->
                if (i == wordPosition)
                    w.copy(syntaxRole = word.syntaxRole)
                else w
            }
        )

        return applyProsodyParadigm(currentClause, wordPosition, word) to wordPosition
    }

    private fun useCategoryApplicator(
        wordSequence: WordSequence,
        wordPosition: Int,
        exponenceCluster: ExponenceCluster,
        exponenceValue: ExponenceValue,
        actualValues: List<SourcedCategoryValue>
    ): WordSequence {
        val word = wordSequence[wordPosition]
        return if (applicators[exponenceCluster]?.containsKey(exponenceValue) == true)
            applicators[exponenceCluster]
                ?.get(exponenceValue)
                ?.apply(wordSequence, wordPosition, actualValues)
                ?: throw ChangeException(
                    "Tried to change word \"$word\" for categories ${exponenceValue.categoryValues.joinToString()} " +
                            "but such Exponence Cluster isn't defined in Language"
                )
        else WordSequence(listOf(word.copy()))
    }

    private fun getExponenceUnion(
        categoryValues: Set<SourcedCategoryValue>,
        exponenceCluster: ExponenceCluster
    ): ExponenceValue? {
        return exponenceCluster.filterExponenceUnion(categoryValues)
    }

    private fun applyProsodyParadigm(wordSequence: WordSequence, wordPosition: Int, oldWord: Word) = WordSequence(
        wordSequence.words.subList(0, wordPosition)
                + listOf(prosodyChangeParadigm.apply(oldWord, wordSequence[wordPosition]))
                + wordSequence.words.subList(wordPosition + 1, wordSequence.size)
    )

    fun hasChanges() = applicators.any { it.value.isNotEmpty() }

    override fun toString() = "$speechPart changes on: \n" +
            exponenceClusters
                .map { it to applicators.getValue(it) }
                .joinToString("\n\n") { (c, m) ->
                    "$c:\n" + m.entries
                        .map { it.key.toString() + ": " + it.value }
                        .sortedWith(naturalOrder())
                        .joinToString("\n")
                }
}