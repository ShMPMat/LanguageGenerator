package shmp.language

import shmp.language.categories.Category
import shmp.language.categories.change.CategoryApplicator

class ChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryValues: List<CategoryValue> = getDefaultState(word)): Clause {
        return speechPartChangeParadigms[word.syntaxCore.speechPart]?.apply(word, categoryValues.toSet())
            ?: throw LanguageException("No SpeechPartChangeParadigm for ${word.syntaxCore.speechPart}")
    }

    fun getDefaultState(word: Word): List<CategoryValue> {
        return speechPartChangeParadigms[word.syntaxCore.speechPart]?.exponenceClusters
            ?.flatMap { it.categories }
            ?.filter { it.values.isNotEmpty() }
            ?.map { it.values[0] }
            ?.filter { enum ->
                word.syntaxCore.staticCategories.none { it.parentClassName == enum.parentClassName }
            }
            ?.union(word.syntaxCore.staticCategories)
            ?.toList()
            ?: throw LanguageException("No SpeechPartChangeParadigm for ${word.syntaxCore.speechPart}")
    }

    override fun toString(): String {
        return categories.joinToString("\n") + "\n\n" +
                speechPartChangeParadigms
                    .map { it.value }
                    .filter { it.hasChanges() }
                    .joinToString("\n")
    }
}

class SpeechPartChangeParadigm(
    val speechPart: SpeechPart,
    val exponenceClusters: List<ExponenceCluster>,
    val applicators: Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>>
) {
    fun apply(word: Word, categoryValues: Set<CategoryValue>): Clause {
        if (word.syntaxCore.speechPart != speechPart)
            throw LanguageException(
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
        return currentClause
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

    override fun toString(): String {
        return "$speechPart changes on: \n${applicators.map {
            it.key.toString() + ":\n" + it.value.map { it.key.toString() + ": " + it.value }.joinToString("\n")
        }.joinToString("\n")}"
    }

    fun hasChanges(): Boolean = applicators.any { it.value.isNotEmpty() }
}

class ExponenceCluster(val categories: List<Category>) {
    val possibleValues: Set<ExponenceValue> = constructExponenceUnionSets(categories)
        .map { ExponenceValue(it, this) }
        .toSet()

    fun contains(exponenceValue: ExponenceValue): Boolean {
        for (category in categories)
            if (exponenceValue.categoryValues.count { category.possibleValues.contains(it) } != 1)
                return false
        return exponenceValue.categoryValues.size == categories.size
    }

    fun filterExponenceUnion(categoryValues: Set<CategoryValue>): ExponenceValue? =
        try {
            ExponenceValue(categoryValues.filter { enum ->
                categories.any { it.possibleValues.contains(enum) }
            }.toSet(), this)
        } catch (e: LanguageException) {
            null
        }

    override fun toString(): String {
        return categories.joinToString("\n")
    }
}

private fun constructExponenceUnionSets(categories: List<Category>): Set<Set<CategoryValue>> = //TODO damn mascarade with sets and lists
    if (categories.size == 1)
        categories[0].values.map { setOf(it) }.toSet()
    else {
        val sets = mutableSetOf<Set<CategoryValue>>()
        val recSets = constructExponenceUnionSets(categories.subList(0, categories.lastIndex))
        categories.last().possibleValues
            .forEach { new -> sets.addAll(recSets.map { it.union(setOf(new)) }) }
        sets
    }


data class ExponenceValue(val categoryValues: Set<CategoryValue>, val parentCluster: ExponenceCluster) {
    init {
        if (categoryValues.groupBy { it.parentClassName }.any { it.value.size > 1 })
            throw LanguageException("Tried to create Exponence Value with Category Value from the same Category")
        if (parentCluster.categories.size != categoryValues.size)
            throw LanguageException(
                "Tried to create Exponence Value of size ${categoryValues.size} " +
                        "for Exponence Cluster of size ${parentCluster.categories.size}"
            )
    }

    override fun toString(): String {
        return categoryValues.joinToString()
    }

}