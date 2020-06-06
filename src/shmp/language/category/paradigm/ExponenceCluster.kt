package shmp.language.category.paradigm

import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.category.Category
import shmp.language.category.CategorySource

class ExponenceCluster(
    val categories: List<ParametrizedCategory>,
    possibleValuesSets: Set<List<ParametrizedCategoryValue>>
) {
    val possibleValues: List<ExponenceValue> = possibleValuesSets
        .map { ExponenceValue(it, this) }

    fun contains(exponenceValue: ExponenceValue) = possibleValues.contains(exponenceValue)

    fun filterExponenceUnion(categoryValues: Set<ParametrizedCategoryValue>): ExponenceValue? =
        try {
            val neededValues = categoryValues.filter {
                categories.any { c ->
                    c.containsParametrizedValue(it)
                }
            }
            possibleValues.first { it.categoryValues.containsAll(neededValues) }
        } catch (e: LanguageException) {
            null
        }

    override fun toString() = categories.joinToString("\n")
}

class ExponenceValue(val categoryValues: List<ParametrizedCategoryValue>, val parentCluster: ExponenceCluster) {
    init {
        if (parentCluster.categories.size != categoryValues.groupBy { it.categoryValue.parentClassName }.size)
            throw LanguageException(
                "Tried to create Exponence Value of size ${categoryValues.size} " +
                        "for Exponence Cluster of size ${parentCluster.categories.size}"
            )

        var currentCategoryIndex = 0
        for (value in categoryValues)
            if (!parentCluster.categories[currentCategoryIndex].containsParametrizedValue(value))
                if (parentCluster.categories[currentCategoryIndex + 1].containsParametrizedValue(value))
                    currentCategoryIndex++
                else throw LanguageException(
                    "Category Values in Exponence Value are ordered not in the same as Categories in Exponence Cluster"
                )
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

data class ParametrizedCategory(val category: Category, val source: CategorySource) {
    val actualParametrizedValues = category.actualValues.map { ParametrizedCategoryValue(it, source) }

    fun containsParametrizedValue(value: ParametrizedCategoryValue) =
        category.allPossibleValues.contains(value.categoryValue)

    override fun toString() = category.toString()
}

data class ParametrizedCategoryValue(val categoryValue: CategoryValue, val source: CategorySource) {
    override fun toString() = categoryValue.toString()
}