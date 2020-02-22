package shmp.language

import shmp.language.categories.Articles
import shmp.language.categories.Category
import shmp.language.categories.change.CategoryApplicator
import kotlin.reflect.KClass

class ChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryEnums: List<CategoryEnum> = getDefaultState(word)): Clause {
        return speechPartChangeParadigms[word.syntaxCore.speechPart]?.apply(word, categoryEnums.toSet())
            ?: throw LanguageException("No SpeechPartChangeParadigm for ${word.syntaxCore.speechPart}")
    }

    fun getDefaultState(word: Word): List<CategoryEnum> {
        return speechPartChangeParadigms[word.syntaxCore.speechPart]?.exponenceClusters
            ?.flatMap { it.categories }
            ?.filter { it.categories.isNotEmpty() }
            ?.map { it.categories[0] }
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
    val applicators: Map<ExponenceCluster, Map<ExponenceUnion, CategoryApplicator>>
) {
    fun apply(word: Word, categoryEnums: Set<CategoryEnum>): Clause {
        if (word.syntaxCore.speechPart != speechPart)
            throw LanguageException(
                "SpeechPartChangeParadigm for $speechPart has been given ${word.syntaxCore.speechPart}"
            )
        var currentClause = Clause(listOf(word))
        var currentWord = word
        var wordPosition = 0
        for (exponenceCluster in exponenceClusters) {
            val exponenceUnion = getExponenceUnion(categoryEnums, exponenceCluster) ?: continue
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
        exponenceUnion: ExponenceUnion
    ): Clause {
        val word = clause[wordPosition]
        return if (applicators[exponenceCluster]?.containsKey(exponenceUnion) == true)
            applicators[exponenceCluster]?.get(exponenceUnion)?.apply(clause, wordPosition)
                ?: throw LanguageException(
                    "Tried to change word \"$word\" for categories ${exponenceUnion.categoryEnums.joinToString()} " +
                            "but such exponence cluster isn't defined in Language"
                )
        else Clause(listOf(word.copy()))
    }

    private fun getExponenceUnion(
        categoryEnums: Set<CategoryEnum>,
        exponenceCluster: ExponenceCluster
    ): ExponenceUnion? {
        return exponenceCluster.filterExponenceUnion(categoryEnums)
    }

    override fun toString(): String {
        return "$speechPart changes on: \n${applicators.map {
            it.key.toString() + ":\n" + it.value.map { it.key.toString() + ": " + it.value }.joinToString("\n")
        }.joinToString("\n")}"
    }

    fun hasChanges(): Boolean = applicators.any { it.value.isNotEmpty() }
}

class ExponenceCluster(val categories: List<Category>) {
    val possibleCategories: Set<ExponenceUnion> = constructExponenceUnionSets(categories)
        .map { ExponenceUnion(it.toList(), this) }
        .toSet()

    fun contains(exponenceUnion: ExponenceUnion): Boolean {
        for (category in categories)
            if (exponenceUnion.categoryEnums.count { category.possibleCategories.contains(it) } != 1)
                return false
        return exponenceUnion.categoryEnums.size == categories.size
    }

    fun filterExponenceUnion(categoryEnums: Set<CategoryEnum>): ExponenceUnion? =
        try {
            ExponenceUnion(categoryEnums.filter { enum ->
                categories.any { it.possibleCategories.contains(enum) }
            }, this)
        } catch (e: LanguageException) {
            null
        }

    override fun toString(): String {
        return categories.joinToString()
    }


}

private fun constructExponenceUnionSets(categories: List<Category>): Set<Set<CategoryEnum>> = //TODO damn mascarade with sets and lists
    if (categories.size == 1)
        categories[0].categories.map { setOf(it) }.toSet()
    else {
        val sets = mutableSetOf<Set<CategoryEnum>>()
        val recSets = constructExponenceUnionSets(categories.subList(0, categories.lastIndex))
        categories.last().possibleCategories
            .forEach { new -> sets.addAll(recSets.map { it.union(setOf(new)) }) }
        sets
    }


data class ExponenceUnion(val categoryEnums: List<CategoryEnum>, val parentCluster: ExponenceCluster) {
    init {
        if (categoryEnums.groupBy { it.parentClassName }.any { it.value.size > 1 })
            throw LanguageException("Tried to create ExponenceUnion with CategoryEnums from the same Category")
        if (parentCluster.categories.size != categoryEnums.size)
            throw LanguageException(
                "Tried to create ExponenceUnion of size ${categoryEnums.size} " +
                        "for ExponenceCluster of size ${parentCluster.categories.size}"
            )
    }

    override fun toString(): String {
        return categoryEnums.joinToString()
    }

}