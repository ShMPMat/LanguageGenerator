package shmp.language.categories

import shmp.language.*
import shmp.language.categories.realization.CategoryApplicator
import shmp.language.phonology.prosody.ProsodyChangeParadigm

class ChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryValues: List<CategoryValue> = getDefaultState(word)): Clause =
        speechPartChangeParadigms[word.syntaxCore.speechPart]?.apply(word, categoryValues.toSet())
            ?: throw LanguageException("No SpeechPartChangeParadigm for ${word.syntaxCore.speechPart}")

    fun getDefaultState(word: Word): List<CategoryValue> =
        speechPartChangeParadigms[word.syntaxCore.speechPart]?.exponenceClusters
            ?.flatMap { it.categories }
            ?.filter { it.actualValues.isNotEmpty() }
            ?.map { it.actualValues[0] }
            ?.filter { enum ->
                word.syntaxCore.staticCategories.none { it.parentClassName == enum.parentClassName }
            }
            ?.union(word.syntaxCore.staticCategories)
            ?.toList()
            ?: throw LanguageException("No SpeechPartChangeParadigm for ${word.syntaxCore.speechPart}")

    fun getSpeechPartParadigm(speechPart: SpeechPart) = speechPartChangeParadigms.getValue(speechPart)

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n")
}

class SpeechPartChangeParadigm(
    val speechPart: SpeechPart,
    val exponenceClusters: List<ExponenceCluster>,
    private val applicators: Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>>,
    private val prosodyChangeParadigm: ProsodyChangeParadigm
) {
    fun apply(word: Word, categoryValues: Set<CategoryValue>): Clause {
        if (word.syntaxCore.speechPart != speechPart) throw LanguageException(
            "SpeechPartChangeParadigm for $speechPart has been given ${word.syntaxCore.speechPart}"
        )

        var currentClause = Clause(listOf(word))
        var currentWord = word
        var wordPosition = 0
        for (exponenceCluster in exponenceClusters) {
                val exponenceUnion = getExponenceUnion(categoryValues, exponenceCluster) ?: continue
                val newClause = useCategoryApplicator(currentClause, wordPosition, exponenceCluster, exponenceUnion)
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
        return applyProsodyParadigm(currentClause, wordPosition, word)
    }

    private fun useCategoryApplicator(
        clause: Clause,
        wordPosition: Int,
        exponenceCluster: ExponenceCluster,
        exponenceValue: ExponenceValue
    ): Clause {
        val word = clause[wordPosition]
        return if (applicators[exponenceCluster]?.containsKey(exponenceValue) == true)
            applicators[exponenceCluster]?.get(exponenceValue)?.apply(clause, wordPosition)
                ?: throw LanguageException(
                    "Tried to change word \"$word\" for categories ${exponenceValue.categoryValues.joinToString()} " +
                            "but such Exponence Cluster isn't defined in Language"
                )
        else Clause(listOf(word.copy()))
    }

    private fun getExponenceUnion(
        categoryValues: Set<CategoryValue>,
        exponenceCluster: ExponenceCluster
    ): ExponenceValue? {
        return exponenceCluster.filterExponenceUnion(categoryValues)
    }

    private fun applyProsodyParadigm(clause: Clause, wordPosition: Int, oldWord: Word): Clause {
        return Clause(
            clause.words.subList(0, wordPosition)
                    + listOf(prosodyChangeParadigm.apply(oldWord, clause[wordPosition]))
                    + clause.words.subList(wordPosition + 1, clause.size)
        )
    }

    fun hasChanges(): Boolean = applicators.any { it.value.isNotEmpty() }

    override fun toString() = "$speechPart changes on: \n" +
            applicators.map { (c, m) -> "$c:\n" + m.entries
                    .map { it.key.toString() + ": " + it.value }
                    .sortedWith(naturalOrder())
                    .joinToString("\n")
            }.joinToString("\n\n")
}

class ExponenceCluster(val categories: List<Category>, possibleValuesSets: Set<List<CategoryValue>>) {
    val possibleValues: Set<ExponenceValue> = possibleValuesSets
        .map { ExponenceValue(it, this) }
        .toSet()

    fun contains(exponenceValue: ExponenceValue): Boolean {
        for (category in categories)
            if (exponenceValue.categoryValues.count { category.allPossibleValues.contains(it) } != 1)
                return false
        return exponenceValue.categoryValues.size == categories.size
    }

    fun filterExponenceUnion(categoryValues: Set<CategoryValue>): ExponenceValue? =
        try {
            val neededValues = categoryValues.filter {
                categories.any { c ->
                    c.allPossibleValues.contains(it)
                }
            }
            possibleValues.first { it.categoryValues.containsAll(neededValues) }
        } catch (e: LanguageException) {
            null
        }

    override fun toString() = categories.joinToString("\n")
}

class ExponenceValue(val categoryValues: List<CategoryValue>, val parentCluster: ExponenceCluster) {
    init {
        if (parentCluster.categories.size != categoryValues.groupBy { it.parentClassName }.size)
            throw LanguageException(
                "Tried to create Exponence Value of size ${categoryValues.size} " +
                        "for Exponence Cluster of size ${parentCluster.categories.size}"
            )
        var currentCategoryIndex = 0
        for (category in categoryValues) {
            if (category.parentClassName != parentCluster.categories[currentCategoryIndex].outType)
                if (category.parentClassName == parentCluster.categories[currentCategoryIndex + 1].outType)
                    currentCategoryIndex++
                else throw LanguageException(
                    "Category Values in Exponence Value are ordered not in the same as Categories in Exponence Cluster"
                )
        }
    }

    override fun toString() = categoryValues.joinToString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExponenceValue

        if (categoryValues != other.categoryValues) return false
        if (parentCluster != other.parentCluster) return false

        return true
    }

    override fun hashCode(): Int {
        var result = categoryValues.hashCode()
        result = 31 * result + parentCluster.hashCode()
        return result
    }

}