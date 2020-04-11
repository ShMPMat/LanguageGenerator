package shmp.generator

import shmp.language.*
import shmp.language.categories.*
import shmp.language.categories.realization.*
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
        categoriesAndSupply: List<Pair<Category, CategoryRandomSupplements>>
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
        realizationTypes.forEach { clusterAndRealization ->
            clusterAndRealization.first.exponenceCluster.possibleValues.forEach {
                val categoryEnums = it.categoryValues
                var syntaxCore = categoryEnums[0].syntaxCore
                for (core in categoryEnums.subList(1, it.categoryValues.size).map { syntaxCore }) {
                    syntaxCore = SyntaxCore(
                        syntaxCore.word + core.word,
                        syntaxCore.speechPart,
                        syntaxCore.tags.union(core.tags),
                        syntaxCore.staticCategories.union(core.staticCategories)
                    )
                }
                map.getValue(clusterAndRealization.first.exponenceCluster)[it] = randomCategoryApplicator(
                    decideRealizationType(clusterAndRealization.second, it, clusterAndRealization.first.supplements),
                    phoneticRestrictions,
                    syntaxCore
                )

            }
        }
        return map
    }

    private fun decideRealizationType(
        categoryRealization: CategoryRealization,
        value: ExponenceValue,
        supplements: List<CategoryRandomSupplements>
    ): CategoryRealization {
        val variants = supplements
            .map { it.specialRealization(value.categoryValues) }
        val finalVariants = uniteMutualProbabilities(variants) { this.copy(probability = it) }
        return randomElement(finalVariants, random).realization ?: categoryRealization
    }

    private fun splitCategoriesOnClusters(
        categoriesAndSupply: List<Pair<Category, CategoryRandomSupplements>>
    ): List<ExponenceTemlate> {
        val shuffledMappers = categoriesAndSupply.shuffled(random)
        val clusters = ArrayList<ExponenceTemlate>()
        var l = 0
        val data = mutableListOf<List<RealizationMapper>>()
        while (l < shuffledMappers.size) {
            val r = randomElement(l + 1..shuffledMappers.size, { 1.0 / it }, random)
            val categories = shuffledMappers.subList(l, r).map { it.first }
            val cluster = ExponenceCluster(
                categories,
                constructExponenceUnionSets(categories)
            )
            data.add(shuffledMappers.subList(l, r).map { it.second::realizationTypeProbability })
            val mapper = { i: Int, c: CategoryRealization ->
                data[i].map { it(c) }.foldRight(0.0, Double::plus)
            }
            clusters.add(ExponenceTemlate(
                cluster,
                mapper,
                shuffledMappers.subList(l, r).map { it.second }
            ))
            l = r
        }
        return clusters
    }

    private fun randomCategoryApplicator(
        realizationType: CategoryRealization,
        phoneticRestrictions: PhoneticRestrictions,
        syntaxCore: SyntaxCore
    ): CategoryApplicator = when (realizationType) {
        CategoryRealization.PrefixSeparateWord -> PrefixWordCategoryApplicator(lexisGenerator.randomWord(
            syntaxCore,
            maxSyllableLength = 3,
            lengthWeight = { ((3 * 3 + 1 - it * it) * (3 * 3 + 1 - it * it)).toDouble() }
        ))
        CategoryRealization.SuffixSeparateWord -> SuffixWordCategoryApplicator(lexisGenerator.randomWord(
            syntaxCore,
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
    }

    fun randomApplicatorsOrder(
        applicators: Map<ExponenceCluster,
                Map<ExponenceValue, CategoryApplicator>>
    ): List<ExponenceCluster> = applicators.keys.shuffled(random)

    private fun constructExponenceUnionSets(
        categories: List<Category>,
        neighbourCategories: BoxedInt = BoxedInt(1)
    ): Set<List<CategoryValue>> =
        if (categories.size == 1)
            if (neighbourCategories.value > 1 && testProbability(categoryCollapseProbability, random)) {
                neighbourCategories.value--
                setOf(categories.first().values.toList())
            } else
                categories.first().values.map { listOf(it) }.toSet()
        else {
            val currentCategory = categories.last()
            val lists = mutableSetOf<List<CategoryValue>>()
            if (neighbourCategories.value > 1 && testProbability(categoryCollapseProbability, random)) {
                neighbourCategories.value--
                val recSets = constructExponenceUnionSets(
                    categories.dropLast(1),
                    BoxedInt(neighbourCategories.value * currentCategory.values.size)
                )
                lists.addAll(recSets.map {
                    val list = ArrayList(it)
                    list.addAll(currentCategory.values)
                    list
                })
            } else {
                val box = BoxedInt(neighbourCategories.value * currentCategory.values.size)
                currentCategory.values.forEach { new ->
                    val recSets = constructExponenceUnionSets(
                        categories.dropLast(1),
                        box
                    )
                    lists.addAll(recSets.map {
                        val list = ArrayList(it)
                        list.add(new)
                        list
                    })
                }
            }
            lists
        }
}

private data class BoxedInt(var value: Int)

typealias RealizationMapper = (CategoryRealization) -> Double

data class ExponenceTemlate(
    val exponenceCluster: ExponenceCluster,
    val mapper: (Int, CategoryRealization) -> Double,
    val supplements: List<CategoryRandomSupplements>
)

fun <E: SampleSpaceObject> uniteMutualProbabilities(
    objectLists: List<Collection<E>>,
    copy: E.(Double) -> E
): List<E> {
    var previousVariants = objectLists.first().toMutableSet()
    var finalVariants = mutableSetOf<E>()
    for (variantList in objectLists.drop(1)) {
        for (variant in variantList) {
            var same = previousVariants.firstOrNull { it == variant} ?: continue
            finalVariants.add(same.copy(same.probability * variant.probability))
        }
        previousVariants = finalVariants
        finalVariants = mutableSetOf()
    }
    return previousVariants.toList()
}