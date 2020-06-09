package shmp.language.category.paradigm

import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.category.realization.CategoryApplicator
import shmp.language.phonology.prosody.ProsodyChangeParadigm
import shmp.language.syntax.Clause

class SpeechPartChangeParadigm(
    val speechPart: SpeechPart,
    val exponenceClusters: List<ExponenceCluster>,
    val applicators: Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>>,
    val prosodyChangeParadigm: ProsodyChangeParadigm
) {
    val categories = exponenceClusters.flatMap { it.categories }

    fun apply(word: Word, categoryValues: Set<ParametrizedCategoryValue>): Pair<Clause, Int> {
        if (word.semanticsCore.speechPart != speechPart) throw ChangeException(
            "SpeechPartChangeParadigm for $speechPart has been given ${word.semanticsCore.speechPart}"
        )

        var currentClause = Clause(listOf(word))
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
        return applyProsodyParadigm(currentClause, wordPosition, word) to wordPosition
    }

    private fun useCategoryApplicator(
        clause: Clause,
        wordPosition: Int,
        exponenceCluster: ExponenceCluster,
        exponenceValue: ExponenceValue,
        actualValues: List<ParametrizedCategoryValue>
    ): Clause {
        val word = clause[wordPosition]
        return if (applicators[exponenceCluster]?.containsKey(exponenceValue) == true)
            applicators[exponenceCluster]
                ?.get(exponenceValue)
                ?.apply(clause, wordPosition, actualValues)
                ?: throw ChangeException(
                    "Tried to change word \"$word\" for categories ${exponenceValue.categoryValues.joinToString()} " +
                            "but such Exponence Cluster isn't defined in Language"
                )
        else Clause(listOf(word.copy()))
    }

    private fun getExponenceUnion(
        categoryValues: Set<ParametrizedCategoryValue>,
        exponenceCluster: ExponenceCluster
    ): ExponenceValue? {
        return exponenceCluster.filterExponenceUnion(categoryValues)
    }

    private fun applyProsodyParadigm(clause: Clause, wordPosition: Int, oldWord: Word) = Clause(
        clause.words.subList(0, wordPosition)
                + listOf(prosodyChangeParadigm.apply(oldWord, clause[wordPosition]))
                + clause.words.subList(wordPosition + 1, clause.size)
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