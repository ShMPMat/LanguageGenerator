package shmp.lang.language.category.paradigm

import shmp.lang.language.CategoryValue
import shmp.lang.language.LanguageException
import shmp.lang.language.category.Category
import shmp.lang.language.category.CategorySource
import shmp.lang.utils.notEqualsByElement


class ExponenceCluster(
    val categories: List<SourcedCategory>,
    possibleValuesSets: Set<List<SourcedCategoryValue>>
) {
    val possibleValues: List<ExponenceValue> = possibleValuesSets
        .map { ExponenceValue(it, this) }

    fun contains(exponenceValue: ExponenceValue) = possibleValues.contains(exponenceValue)

    fun filterExponenceUnion(categoryValues: Set<SourcedCategoryValue>): ExponenceValue? {
        return try {
            if (categoryValues.isEmpty())
                return null

            val neededValues = categoryValues
                .filter { v -> categories.any { it.containsValue(v) } }

            if (neededValues.isEmpty())
                return null

            possibleValues.firstOrNull { it.categoryValues.containsAll(neededValues) }
                ?: throw LanguageException("No exponence cluster for $neededValues")
        } catch (e: LanguageException) {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExponenceCluster) return false

        if (categories != other.categories) return false
        if (possibleValues notEqualsByElement other.possibleValues) return false

        return true
    }

    override fun hashCode(): Int {
        var result = categories.hashCode()
        result = 31 * result + possibleValues.hashCode()
        return result
    }



    override fun toString() = categories.joinToString("\n")
}

class ExponenceValue(val categoryValues: List<SourcedCategoryValue>, val parentCluster: ExponenceCluster) {
    init {
        val valueTypesAmount = categoryValues.groupBy { it.categoryValue.parentClassName to it.source }.size

        if (parentCluster.categories.size != valueTypesAmount)
            throw LanguageException(
                "Tried to create Exponence Value of size ${categoryValues.size} " +
                        "for Exponence Cluster of size ${parentCluster.categories.size}"
            )

        var currentCategoryIndex = 0
        for (value in categoryValues)
            if (!parentCluster.categories[currentCategoryIndex].containsValue(value))
                if (parentCluster.categories[currentCategoryIndex + 1].containsValue(value))
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
        if (parentCluster.categories!= other.parentCluster.categories) return false

        return true
    }

    override fun hashCode(): Int {
        var result = categoryValues.hashCode()
        result = 31 * result + parentCluster.categories.hashCode()
        return result
    }
}

data class SourcedCategory(val category: Category, val source: CategorySource, var compulsoryData: CompulsoryData) {
    val allPossibleSourcedValues = category.allPossibleValues.map { SourcedCategoryValue(it, source, this) }
    val actualSourcedValues = category.actualValues.map { SourcedCategoryValue(it, source, this) }

    fun containsValue(value: SourcedCategoryValue) = allPossibleSourcedValues.contains(value)

    override fun toString(): String {
        val categoriesString = category.toString() + getSourceString(source)
        val compulsoryString = if (compulsoryData.isCompulsory)
            "Compulsory " + (
                    if (compulsoryData.compulsoryCoCategories.isNotEmpty())
                        compulsoryData.compulsoryCoCategories.joinToString(", ", "for ", " ")
                    else ""
                    )
        else "Optional "

        return compulsoryString + categoriesString
    }
}

data class SourcedCategoryValue(val categoryValue: CategoryValue, val source: CategorySource, val parent: SourcedCategory) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SourcedCategoryValue

        if (categoryValue != other.categoryValue) return false
        if (source != other.source) return false

        return true
    }

    override fun hashCode(): Int {
        var result = categoryValue.hashCode()
        result = 31 * result + source.hashCode()
        return result
    }

    override fun toString() = categoryValue.alias + getSourceString(source)
}


data class CompulsoryData(val isCompulsory: Boolean, val compulsoryCoCategories: List<CategoryCluster>) {
    fun isApplicable(values: List<CategoryValue>) = compulsoryCoCategories
        .all { it.any { cc -> values.contains(cc) } }

    fun mustExist(values: List<CategoryValue>) = isCompulsory && isApplicable(values)
}

typealias CategoryCluster = List<CategoryValue>

infix fun Boolean.withCoCategories(coCategories: List<CategoryCluster>) =
    CompulsoryData(this, coCategories)


typealias SourcedCategoryValues = List<SourcedCategoryValue>

private fun getSourceString(source: CategorySource) =
    if (source is CategorySource.Agreement)
        " ${source.relation.shortName}"
    else ""
