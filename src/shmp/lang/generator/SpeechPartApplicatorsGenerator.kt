package shmp.lang.generator

import shmp.lang.language.CategoryRealization
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.ExponenceValue
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.category.realization.*
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.morphem.Prefix
import shmp.lang.language.morphem.Suffix
import shmp.lang.language.morphem.change.Position
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomUnwrappedElement
import shmp.random.singleton.testProbability
import kotlin.random.Random


class SpeechPartApplicatorsGenerator(
    private val lexisGenerator: LexisGenerator,
    private val changeGenerator: ChangeGenerator,
    private val random: Random
) {
    private val categoryCollapseProbability = 0.5

    internal fun randomApplicatorsForSpeechPart(
        speechPart: TypedSpeechPart,
        phoneticRestrictions: PhoneticRestrictions,
        categoriesAndSupply: List<Pair<SourcedCategory, CategoryRandomSupplements>>
    ): Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>> {
        val map = HashMap<ExponenceCluster, MutableMap<ExponenceValue, CategoryApplicator>>()

        val exponenceTemplates = splitCategoriesOnClusters(categoriesAndSupply)
        exponenceTemplates.forEach { map[it.exponenceCluster] = mutableMapOf() }

        val realizationTypes = exponenceTemplates
            .mapIndexed { i, t -> t to CategoryRealization.values().randomElement { c -> t.mapper(i, c) } }

        for ((cluster, realization) in realizationTypes) {
            cluster.exponenceCluster.possibleValues.forEach { exponenceValue ->
                val categoryEnums = exponenceValue.categoryValues
                var semanticsCore = categoryEnums[0].categoryValue.semanticsCore
                val chosenEnums = categoryEnums.subList(1, exponenceValue.categoryValues.size)
                    .map { semanticsCore }

                for (core in chosenEnums) {
                    semanticsCore = SemanticsCore(
                        semanticsCore.meaningCluster + core.meaningCluster,
                        semanticsCore.speechPart,
                        semanticsCore.tags.union(core.tags),
                        semanticsCore.derivationCluster,
                        semanticsCore.staticCategories.union(core.staticCategories)
                    )
                }
                map.getValue(cluster.exponenceCluster)[exponenceValue] = randomCategoryApplicator(
                    decideRealizationType(
                        realization,
                        exponenceValue,
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
        speechPart: TypedSpeechPart
    ): CategoryRealization {
        val variants = supplements
            .map { s -> s.specialRealization(value.categoryValues.map { it.categoryValue }, speechPart.type) }
        val finalVariants = uniteMutualProbabilities(variants) { this.copy(probability = it) }

        return finalVariants.randomUnwrappedElement()
            ?: categoryRealization
    }

    private fun splitCategoriesOnClusters(
        categories: List<Pair<SourcedCategory, CategoryRandomSupplements>>
    ): List<ExponenceTemplate> {
        val shuffledCategories = categories.shuffled(random)
        val clusters = ArrayList<ExponenceTemplate>()
        var l = 0
        val data = mutableListOf<List<RealizationMapper>>()

        while (l < shuffledCategories.size) {
            val r = (l + 1..shuffledCategories.size).toList().randomElement { 1.0 / it }
            val currentCategoriesWithSupplement = shuffledCategories.subList(l, r)
            val currentCategories = currentCategoriesWithSupplement.map { it.first }

            val cluster = ExponenceCluster(currentCategories, constructExponenceUnionSets(currentCategories))

            data.add(currentCategoriesWithSupplement.map { it.second::realizationTypeProbability })
            val mapper = { i: Int, c: CategoryRealization ->
                data[i].map { it(c) }.foldRight(0.0, Double::plus)
            }
            clusters.add(ExponenceTemplate(
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
        CategoryRealization.PrefixSeparateWord -> PrefixWordCategoryApplicator(lexisGenerator.generateWord(
            semanticsCore
        ))
        CategoryRealization.SuffixSeparateWord -> SuffixWordCategoryApplicator(lexisGenerator.generateWord(
            semanticsCore
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
        CategoryRealization.NewWord -> NewWordCategoryApplicator(lexisGenerator.generateWord(semanticsCore))
    }

    fun randomApplicatorsOrder(
        applicators: Map<ExponenceCluster,
                Map<ExponenceValue, CategoryApplicator>>
    ): List<ExponenceCluster> = applicators.keys.shuffled(random)

    private fun constructExponenceUnionSets(
        categories: List<SourcedCategory>,
        neighbourCategories: BoxedInt = BoxedInt(1)
    ): Set<List<SourcedCategoryValue>> =
        if (categories.size == 1)
            makeTrivialExponenceUnionSets(categories.first(), neighbourCategories)
        else
            makeRecursiveExponenceUnionSets(categories, neighbourCategories)

    private fun makeTrivialExponenceUnionSets(
        category: SourcedCategory,
        neighbourCategories: BoxedInt
    ): Set<List<SourcedCategoryValue>> =
        if (neighbourCategories.value > 1 && categoryCollapseProbability.testProbability()) {
            neighbourCategories.value--
            setOf(category.actualSourcedValues)
        } else
            category.actualSourcedValues.map { listOf(it) }.toSet()

    private fun makeRecursiveExponenceUnionSets(
        categories: List<SourcedCategory>,
        neighbourCategories: BoxedInt
    ): Set<List<SourcedCategoryValue>> {
        val currentCategory = categories.last()

        return if (neighbourCategories.value > 1 && categoryCollapseProbability.testProbability())
            makeCollapsedExponenceUnionSets(currentCategory, categories.dropLast(1), neighbourCategories)
        else
            makeNonCollapsedExponenceUnionSets(currentCategory, categories.dropLast(1), neighbourCategories)
    }

    private fun makeCollapsedExponenceUnionSets(
        currentCategory: SourcedCategory,
        categories: List<SourcedCategory>,
        neighbourCategories: BoxedInt
    ): Set<List<SourcedCategoryValue>> {
        neighbourCategories.value--
        val existingPaths = BoxedInt(neighbourCategories.value * currentCategory.actualSourcedValues.size)
        val recSets = constructExponenceUnionSets(categories, existingPaths)

        return recSets.map { it + currentCategory.actualSourcedValues }.toSet()
    }

    private fun makeNonCollapsedExponenceUnionSets(
        currentCategory: SourcedCategory,
        categories: List<SourcedCategory>,
        neighbourCategories: BoxedInt
    ): Set<List<SourcedCategoryValue>> {
        val lists = mutableSetOf<List<SourcedCategoryValue>>()

        for (new in currentCategory.actualSourcedValues) {
            val recSets = constructExponenceUnionSets(
                categories,
                BoxedInt(neighbourCategories.value * currentCategory.actualSourcedValues.size)
            )
            lists.addAll(recSets.map { it + listOf(new) })
        }

        return lists
    }
}

private data class BoxedInt(var value: Int)

typealias RealizationMapper = (CategoryRealization) -> Double

data class ExponenceTemplate(
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
