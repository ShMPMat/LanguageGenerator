package shmp.generator

import shmp.language.*
import shmp.language.category.*
import shmp.language.category.paradigm.ExponenceCluster
import shmp.language.category.paradigm.ExponenceValue
import shmp.language.category.paradigm.ParametrizedCategory
import shmp.language.category.paradigm.ParametrizedCategoryValue
import shmp.language.category.realization.*
import shmp.language.lexis.SemanticsCore
import shmp.language.morphem.*
import shmp.language.morphem.change.Position
import shmp.language.phonology.PhoneticRestrictions
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.testProbability
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

class SpeechPartApplicatorsGenerator(
    private val lexisGenerator: LexisGenerator,
    private val changeGenerator: ChangeGenerator,
    private val random: Random
) {
    private val categoryCollapseProbability = 0.5

    internal fun randomApplicatorsForSpeechPart(
        speechPart: SpeechPart,
        phoneticRestrictions: PhoneticRestrictions,
        categoriesAndSupply: List<Pair<ParametrizedCategory, CategoryRandomSupplements>>
    ): Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>> {
        val map = HashMap<ExponenceCluster, MutableMap<ExponenceValue, CategoryApplicator>>()
        val exponenceTemplates = splitCategoriesOnClusters(categoriesAndSupply)
        exponenceTemplates.forEach { map[it.exponenceCluster] = HashMap() }

        val realizationTypes = exponenceTemplates.zip(exponenceTemplates.indices)
            .map {
                it.first to randomElement(
                    CategoryRealization.values(),
                    { c -> it.first.mapper(it.second, c) },
                    random
                )
            }
        realizationTypes.forEach { (cluster, realization) ->
            cluster.exponenceCluster.possibleValues.forEach {
                val categoryEnums = it.categoryValues
                var semanticsCore = categoryEnums[0].categoryValue.semanticsCore
                for (core in categoryEnums.subList(1, it.categoryValues.size).map { semanticsCore }) {
                    semanticsCore = SemanticsCore(
                        semanticsCore.word + core.word,
                        semanticsCore.speechPart,
                        semanticsCore.tags.union(core.tags),
                        semanticsCore.staticCategories.union(core.staticCategories)
                    )
                }
                map.getValue(cluster.exponenceCluster)[it] = randomCategoryApplicator(
                    decideRealizationType(
                        realization,
                        it,
                        cluster.supplements,
                        speechPart
                    ),
                    phoneticRestrictions,
                    semanticsCore
                )

            }
        }
        return map
    }

    private fun decideRealizationType(
        categoryRealization: CategoryRealization,
        value: ExponenceValue,
        supplements: List<CategoryRandomSupplements>,
        speechPart: SpeechPart
    ): CategoryRealization {
        val variants = supplements
            .map { it.specialRealization(value.categoryValues.map { it.categoryValue }, speechPart) }
        val finalVariants = uniteMutualProbabilities(variants) { this.copy(probability = it) }
        return randomElement(finalVariants, random).realization ?: categoryRealization
    }

    private fun splitCategoriesOnClusters(
        categories: List<Pair<ParametrizedCategory, CategoryRandomSupplements>>
    ): List<ExponenceTemlate> {
        val shuffledCategories = categories.shuffled(random)
        val clusters = ArrayList<ExponenceTemlate>()
        var l = 0
        val data = mutableListOf<List<RealizationMapper>>()
        while (l < shuffledCategories.size) {
            val r = randomElement(l + 1..shuffledCategories.size, { 1.0 / it }, random)
            val currentCategoriesWithSupplement = shuffledCategories.subList(l, r)
            val currentCategories = currentCategoriesWithSupplement.map { it.first }
            val cluster = ExponenceCluster(
                currentCategories,
                constructExponenceUnionSets(currentCategories)
            )
            data.add(currentCategoriesWithSupplement.map { it.second::realizationTypeProbability })
            val mapper = { i: Int, c: CategoryRealization ->
                data[i].map { it(c) }.foldRight(0.0, Double::plus)
            }
            clusters.add(ExponenceTemlate(
                cluster,
                mapper,
                currentCategoriesWithSupplement.map { it.second }
            ))
            l = r
        }
        return clusters
    }

    private fun randomCategoryApplicator(
        realizationType: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        semanticsCore: SemanticsCore
    ): CategoryApplicator = when (realizationType) {
        CategoryRealization.PrefixSeparateWord -> PrefixWordCategoryApplicator(lexisGenerator.randomWord(
            semanticsCore,
            maxSyllableLength = 3,
            lengthWeight = { ((3 * 3 + 1 - it * it) * (3 * 3 + 1 - it * it)).toDouble() }
        ))
        CategoryRealization.SuffixSeparateWord -> SuffixWordCategoryApplicator(lexisGenerator.randomWord(
            semanticsCore,
            maxSyllableLength = 3,
            lengthWeight = { ((3 * 3 + 1 - it * it) * (3 * 3 + 1 - it * it)).toDouble() }
        ))
        CategoryRealization.Prefix -> {
            val changes = changeGenerator.generateChanges(Position.Beginning, phoneticRestrictions)
            AffixCategoryApplicator(
                Prefix(changes),
                CategoryRealization.Prefix
            )
        }
        CategoryRealization.Suffix -> {
            val change = changeGenerator.generateChanges(Position.End, phoneticRestrictions)
            AffixCategoryApplicator(
                Suffix(change),
                CategoryRealization.Suffix
            )
        }
        CategoryRealization.Reduplication -> ReduplicationCategoryApplicator()
        CategoryRealization.Passing -> PassingCategoryApplicator()
        CategoryRealization.NewWord -> NewWordCategoryApplicator(lexisGenerator.randomWord(semanticsCore))
    }

    fun randomApplicatorsOrder(
        applicators: Map<ExponenceCluster,
                Map<ExponenceValue, CategoryApplicator>>
    ): List<ExponenceCluster> = applicators.keys.shuffled(random)

    private fun constructExponenceUnionSets(
        categories: List<ParametrizedCategory>,
        neighbourCategories: BoxedInt = BoxedInt(1)
    ): Set<List<ParametrizedCategoryValue>> =
        if (categories.size == 1)
            makeTrivialExponenceUnionSets(categories.first(), neighbourCategories)
        else
            makeRecursiveExponenceUnionSets(categories, neighbourCategories)

    private fun makeTrivialExponenceUnionSets(
        category: ParametrizedCategory,
        neighbourCategories: BoxedInt
    ): Set<List<ParametrizedCategoryValue>> =
        if (neighbourCategories.value > 1 && testProbability(categoryCollapseProbability, random)) {
            neighbourCategories.value--
            setOf(category.actualParametrizedValues)
        } else
            category.actualParametrizedValues.map { listOf(it) }.toSet()

    private fun makeRecursiveExponenceUnionSets(
        categories: List<ParametrizedCategory>,
        neighbourCategories: BoxedInt
    ): Set<List<ParametrizedCategoryValue>> {
        val currentCategory = categories.last()

        return if (neighbourCategories.value > 1 && testProbability(categoryCollapseProbability, random))
            makeCollapsedExponenceUnionSets(currentCategory, categories.dropLast(1), neighbourCategories)
        else
            makeNonCollapsedExponenceUnionSets(currentCategory, categories.dropLast(1), neighbourCategories)
    }

    private fun makeCollapsedExponenceUnionSets(
        currentCategory: ParametrizedCategory,
        categories: List<ParametrizedCategory>,
        neighbourCategories: BoxedInt
    ): Set<List<ParametrizedCategoryValue>> {
        neighbourCategories.value--
        val existingPaths = BoxedInt(neighbourCategories.value * currentCategory.actualParametrizedValues.size)
        val recSets = constructExponenceUnionSets(categories, existingPaths)

        return recSets.map {
            val list = it.toMutableList()
            list.addAll(currentCategory.actualParametrizedValues)
            list
        }.toSet()
    }

    private fun makeNonCollapsedExponenceUnionSets(
        currentCategory: ParametrizedCategory,
        categories: List<ParametrizedCategory>,
        neighbourCategories: BoxedInt
    ): Set<List<ParametrizedCategoryValue>> {
        val lists = mutableSetOf<List<ParametrizedCategoryValue>>()

        for (new in currentCategory.actualParametrizedValues) {
            val recSets = constructExponenceUnionSets(
                categories,
                BoxedInt(neighbourCategories.value * currentCategory.actualParametrizedValues.size)
            )
            lists.addAll(recSets.map {
                val list = it.toMutableList()
                list.add(new)
                list
            })
        }

        return lists
    }
}

private data class BoxedInt(var value: Int)

typealias RealizationMapper = (CategoryRealization) -> Double

data class ExponenceTemlate(
    val exponenceCluster: ExponenceCluster,
    val mapper: (Int, CategoryRealization) -> Double,
    val supplements: List<CategoryRandomSupplements>
)

fun <E : SampleSpaceObject> uniteMutualProbabilities(
    objectLists: List<Collection<E>>,
    copy: E.(Double) -> E
): List<E> {
    var previousVariants = objectLists.first().toMutableSet()
    var newVariants = mutableSetOf<E>()
    for (variantList in objectLists.drop(1)) {
        for (variant in variantList) {
            val same = previousVariants.firstOrNull { it == variant }
            if (same == null)
                newVariants.add(variant)
            else
                newVariants.add(same.copy(same.probability * variant.probability))
        }
        previousVariants = newVariants
        newVariants = mutableSetOf()
    }
    return previousVariants.toList()
}