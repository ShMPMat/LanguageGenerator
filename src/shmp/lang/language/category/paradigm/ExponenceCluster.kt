package shmp.lang.language.category.paradigm

import shmp.lang.language.CategoryValue
import shmp.lang.language.LanguageException
import shmp.lang.language.category.Category
import shmp.lang.language.category.CategorySource


class ExponenceCluster(
    val categories: List<ParametrizedCategory>,
    possibleValuesSets: Set<List<ParametrizedCategoryValue>>
) {
    val possibleValues: List<ExponenceValue> = possibleValuesSets
        .map { ExponenceValue(it, this) }

    fun contains(exponenceValue: ExponenceValue) = possibleValues.contains(exponenceValue)

    fun filterExponenceUnion(categoryValues: Set<ParametrizedCategoryValue>): ExponenceValue? =
        try {
            val neededValues = categoryValues.filter { v ->
                categories.any { it.containsParametrizedValue(v) }
            }
            possibleValues.firstOrNull { it.categoryValues.containsAll(neededValues) } ?: kotlin.run {
                null
            }
        } catch (e: LanguageException) {
            null
        }

    override fun toString() = categories.joinToString("\n")
}

class ExponenceValue(val categoryValues: List<ParametrizedCategoryValue>, val parentCluster: ExponenceCluster) {
    init {
        val valueTypesAmount = categoryValues.groupBy { it.categoryValue.parentClassName to it.source }.size

        if (parentCluster.categories.size != valueTypesAmount)
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
                    "Category Values in Exponence Value are ordered not in the same as  Categories in Exponence Cluster"
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
    val allPossibleParametrizedValues = category.allPossibleValues.map { ParametrizedCategoryValue(it, source) }
    val actualParametrizedValues = category.actualValues.map { ParametrizedCategoryValue(it, source) }

    fun containsParametrizedValue(value: ParametrizedCategoryValue) = allPossibleParametrizedValues.contains(value)

    override fun toString() = category.toString() + getSourceString(source)
}

data class ParametrizedCategoryValue(val categoryValue: CategoryValue, val source: CategorySource) {
    override fun toString() = categoryValue.toString() + getSourceString(source)
}

typealias ParametrizedCategoryValues = List<ParametrizedCategoryValue>

fun CategoryValue.parametrize(source: CategorySource) =
    ParametrizedCategoryValue(this, source)


private fun getSourceString(source: CategorySource) =
    if (source is CategorySource.RelationGranted)
        " from ${source.relation}"
    else ""
