package shmp.language.category.paradigm

import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.category.Category

class ExponenceCluster(val categories: List<Category>, possibleValuesSets: Set<List<CategoryValue>>) {
    val possibleValues: List<ExponenceValue> = possibleValuesSets
        .map { ExponenceValue(it, this) }

    fun contains(exponenceValue: ExponenceValue) = possibleValues.contains(exponenceValue)

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