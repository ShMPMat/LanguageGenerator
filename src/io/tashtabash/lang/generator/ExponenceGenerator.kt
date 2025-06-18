package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.category.realization.CategoryRealization.Suppletion
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.category.CategoryRandomSupplements
import io.tashtabash.lang.language.category.value.RealizationBox
import io.tashtabash.lang.language.category.paradigm.ExponenceCluster
import io.tashtabash.lang.language.category.paradigm.ExponenceValue
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.category.realization.CategoryRealization.Passing
import io.tashtabash.lang.language.category.realization.categoryRealizationClusters
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomUnwrappedElementOrNull
import io.tashtabash.random.singleton.testProbability


class ExponenceGenerator {
    private val categoryCollapseProbability = 0.5
    private val keepNonPreferableRealizationProb = 0.01

    // Defines whether the lang tends to use Suffixes or Prefixes
    private val preferableRealizations = categoryRealizationClusters
        .map { it to it.randomElement() }

    // If the language tends to use Suffixes, stochastically return Suffix if realization = Prefix & vice versa
    private fun usePreferableRealization(realization: CategoryRealization): CategoryRealization =
        preferableRealizations.firstOrNull { realization in it.first }
            ?.second
            ?.takeIf { (1 - keepNonPreferableRealizationProb).testProbability() }
            ?: realization

    internal fun splitCategoriesOnClusters(
        categories: List<SupplementedSourcedCategory>,
        speechPart: TypedSpeechPart
    ): List<ExponenceTemplate> {
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
            val supplements = currentCategoriesWithSupplement.map { it.second }
            val currentCategories = currentCategoriesWithSupplement.map { it.first }

            val cluster = ExponenceCluster(
                currentCategories,
                constructExponenceUnionSets(currentCategoriesWithSupplement)
            )
            val realizations = generateRealizationTemplate(
                currentCategoriesWithSupplement,
                cluster,
                speechPart,
                clusters.size
            )

            clusters += ExponenceTemplate(cluster, realizations, supplements)
            l = r
        }

        return clusters
    }

    internal fun generateRealizationTemplate(
        currentCategoriesWithSupplement: List<SupplementedSourcedCategory>,
        cluster: ExponenceCluster,
        speechPart: TypedSpeechPart,
        order: Int
    ): RealizationTemplate {
        val supplements = currentCategoriesWithSupplement.map { it.second }
        val currentCategories = currentCategoriesWithSupplement.map { it.first }
        val areCategoriesCompulsory = currentCategories.all { it.compulsoryData.isCompulsory }

        val mapper = makeMapper(currentCategoriesWithSupplement, order)
        val default = CategoryRealization.entries.randomElement { mapper(it) }
        val realizationTemplate = mutableMapOf<ExponenceValue, Realizations>()
        for (value in cluster.possibleValues) {
            val possibleTypes = getRealizationTypes(value, supplements, speechPart, currentCategories, order)
                // Exclude the probability of Passing types if the categories are optional (makes no sense)
                .filter { areCategoriesCompulsory || it.realization != Passing }
            val rawType = possibleTypes.randomUnwrappedElementOrNull()
                ?: default
            val chosenType = usePreferableRealization(rawType)

            realizationTemplate[value] = Realizations(chosenType, possibleTypes)
        }

        return realizationTemplate
    }

    private fun getRealizationTypes(
        value: ExponenceValue,
        supplements: List<CategoryRandomSupplements>,
        speechPart: TypedSpeechPart,
        categories: List<SourcedCategory>,
        position: Int
    ): List<RealizationBox> {
        val categoryValues: CategoryValues = value.categoryValues.map { it.categoryValue }
        val variants: List<Set<RealizationBox>> = supplements.map {
            it.specialRealization(categoryValues, speechPart.type, categories)
        }

        return uniteMutualProbabilities(variants) { copy(probability = it) }
            .filter { position == 0 || it.realization != Suppletion }
    }

    private fun makeMapper(
        currentCategoriesWithSupplement: List<SupplementedSourcedCategory>,
        i: Int
    ) : (CategoryRealization) -> Double {
        return { c: CategoryRealization ->
            if (i == 0 || c != Suppletion)
                currentCategoriesWithSupplement.sumOf { it.second.realizationTypeProbability(c) }
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
    val cluster: ExponenceCluster,
    val realizations: RealizationTemplate,
    val supplements: List<CategoryRandomSupplements>
)

data class Realizations(val chosen: CategoryRealization, val possible: List<RealizationBox>)

typealias RealizationTemplate = Map<ExponenceValue, Realizations>
