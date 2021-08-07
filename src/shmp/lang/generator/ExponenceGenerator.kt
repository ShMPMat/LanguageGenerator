package shmp.lang.generator

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.Suppletion
import shmp.lang.language.CategoryValues
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


class ExponenceGenerator {
    private val categoryCollapseProbability = 0.5

    internal fun splitCategoriesOnClusters(categories: List<SupplementedSourcedCategory>): List<ExponenceTemplate> {
        val shuffledCategories = categories.shuffled(RandomSingleton.random)
            .let { cs ->
                val nonCompulsory = cs.filter { (c) -> !c.compulsoryData.isCompulsory }
                val compulsory = cs.filter { (c) -> c.compulsoryData.isCompulsory }
                nonCompulsory + compulsory
            }
        val clusters = mutableListOf<ExponenceTemplate>()
        var l = 0

        while (l < shuffledCategories.size) {
            val r = if (shuffledCategories[l].first.compulsoryData.isCompulsory)
                (l + 1..shuffledCategories.size).toList().randomElement { 1.0 / it }
            else l + 1

            val currentCategoriesWithSupplement = shuffledCategories.subList(l, r)
                .mapIndexed { i, p ->
                    val externalValues = categories.toMutableList().apply { removeAt(i) }
                        .flatMap { it.first.category.actualValues }

                    p to p.second.getCollapseCoefficient(externalValues)
                }
                .sortedBy { it.second }
                .map { it.first }

            val currentCategories = currentCategoriesWithSupplement.map { it.first }

            val cluster = ExponenceCluster(
                currentCategories,
                constructExponenceUnionSets(currentCategoriesWithSupplement)
            )

            val mapper = makeMapper(currentCategoriesWithSupplement, clusters.size)
            clusters += ExponenceTemplate(cluster, mapper, currentCategoriesWithSupplement.map { it.second })
            l = r
        }

        return clusters
    }

    internal fun makeMapper(
        currentCategoriesWithSupplement: List<SupplementedSourcedCategory>,
        i: Int
    ) : (CategoryRealization) -> Double {
        return { c: CategoryRealization ->
            if (i == 0 || c != Suppletion)
                currentCategoriesWithSupplement.sumByDouble { it.second.realizationTypeProbability(c) }
            else 0.0
        }
    }


    private fun constructExponenceUnionSets(
        categories: List<SupplementedSourcedCategory>,
        previousCategoryValues: CategoryValues = listOf(),
        neighbourCategories: BoxedInt = BoxedInt(1)
    ): Set<List<SourcedCategoryValue>> = if (categories.size == 1)
        makeTrivialExponenceUnionSets(categories.first(), neighbourCategories, previousCategoryValues)
    else
        makeRecursiveExponenceUnionSets(categories, neighbourCategories, previousCategoryValues)

    private fun makeTrivialExponenceUnionSets(
        category: SupplementedSourcedCategory,
        neighbourCategories: BoxedInt,
        previousCategoryValues: CategoryValues
    ) =
        if (neighbourCategories.value > 1 && testCollapse(category.second, previousCategoryValues)) {
            neighbourCategories.value--
            setOf(category.first.actualSourcedValues)
        } else
            category.first.actualSourcedValues.map { listOf(it) }.toSet()

    private fun makeRecursiveExponenceUnionSets(
        categories: List<SupplementedSourcedCategory>,
        neighbourCategories: BoxedInt,
        previousCategoryValues: CategoryValues
    ): Set<List<SourcedCategoryValue>> {
        val (currentCategory, currentSupplement) = categories.last()
        val leftCategories = categories.dropLast(1)

        return if (neighbourCategories.value > 1 && testCollapse(currentSupplement, previousCategoryValues))
            makeCollapsedExponenceUnionSets(
                currentCategory,
                leftCategories,
                neighbourCategories,
                previousCategoryValues
            )
        else
            makeNonCollapsedExponenceUnionSets(
                currentCategory,
                leftCategories,
                neighbourCategories,
                previousCategoryValues
            )
    }

    private fun makeCollapsedExponenceUnionSets(
        currentCategory: SourcedCategory,
        categories: List<SupplementedSourcedCategory>,
        neighbourCategories: BoxedInt,
        previousCategoryValues: CategoryValues
    ): Set<List<SourcedCategoryValue>> {
        neighbourCategories.value--
        val existingPaths = BoxedInt(neighbourCategories.value * currentCategory.actualSourcedValues.size)
        val recSets = constructExponenceUnionSets(
            categories,
            previousCategoryValues + currentCategory.actualSourcedValues.map { it.categoryValue },
            existingPaths
        )

        return recSets.map { it + currentCategory.actualSourcedValues }.toSet()
    }

    private fun makeNonCollapsedExponenceUnionSets(
        currentCategory: SourcedCategory,
        categories: List<SupplementedSourcedCategory>,
        neighbourCategories: BoxedInt,
        previousCategoryValues: CategoryValues
    ): Set<List<SourcedCategoryValue>> {
        val lists = mutableSetOf<List<SourcedCategoryValue>>()

        for (newCategory in currentCategory.actualSourcedValues) {
            val recSets = constructExponenceUnionSets(
                categories,
                previousCategoryValues + newCategory.categoryValue,
                BoxedInt(neighbourCategories.value * currentCategory.actualSourcedValues.size)
            )
            lists += recSets.map { it + newCategory }
        }

        return lists
    }

    private fun testCollapse(supplements: CategoryRandomSupplements, otherCategories: CategoryValues): Boolean {
        val collapseProbability = categoryCollapseProbability /
                supplements.getCollapseCoefficient(otherCategories)

        return collapseProbability.testProbability()
    }
}

internal data class BoxedInt(var value: Int)

data class ExponenceTemplate(
    val exponenceCluster: ExponenceCluster,
    val mapper: (CategoryRealization) -> Double,
    val supplements: List<CategoryRandomSupplements>
)
