package shmp.lang.generator

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.ExponenceValue
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.category.realization.*
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.morphem.Prefix
import shmp.lang.language.morphem.Suffix
import shmp.lang.language.morphem.change.Position
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.random.SampleSpaceObject
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomUnwrappedElement
import shmp.random.singleton.testProbability


class SpeechPartApplicatorsGenerator(
    private val lexisGenerator: LexisGenerator,
    private val changeGenerator: ChangeGenerator
) {
    private val categoryCollapseProbability = 0.5

    internal fun randomApplicatorsForSpeechPart(
        speechPart: TypedSpeechPart,
        phoneticRestrictions: PhoneticRestrictions,
        categoriesAndSupply: List<SupplementedSourcedCategory>
    ): Result {
        val categories = categoriesAndSupply.map { it.first }
        val map = HashMap<ExponenceCluster, MutableMap<ExponenceValue, CategoryApplicator>>()
        val words = mutableListOf<Word>()

        val exponenceTemplates = splitCategoriesOnClusters(categoriesAndSupply)
        exponenceTemplates.forEach { map[it.exponenceCluster] = mutableMapOf() }

        val realizationTypes = exponenceTemplates
            .mapIndexed { i, t -> t to values().randomElement { c -> t.mapper(i, c) } }

        for (i in realizationTypes.indices) {
            val (cluster, realization) = realizationTypes[i]

            cluster.exponenceCluster.possibleValues.forEach { exponenceValue ->
                val cores = exponenceValue.categoryValues
                    .map { it.categoryValue.semanticsCore }
                val semanticsCore = combineSemanticCores(cores)
                val (applicator, word) = randomCategoryApplicator(
                    decideRealizationType(realization, exponenceValue, cluster.supplements, speechPart, categories, i),
                    phoneticRestrictions,
                    semanticsCore
                )
                map.getValue(cluster.exponenceCluster)[exponenceValue] = applicator
                word?.let {
                    words.add(it)
                }
            }
        }
        return Result(words, map)
    }

    private fun combineSemanticCores(semanticsCores: List<SemanticsCore>): SemanticsCore {
        var semanticsCore = semanticsCores[0]
        for (core in semanticsCores.drop(1))
            semanticsCore = SemanticsCore(
                semanticsCore.meaningCluster + core.meaningCluster,
                if (core.speechPart.type != SpeechPart.Particle) core.speechPart else semanticsCore.speechPart,
                semanticsCore.connotations + core.connotations,
                semanticsCore.tags + core.tags,
                semanticsCore.derivationCluster,
                semanticsCore.staticCategories + core.staticCategories
            )
        return semanticsCore
    }

    private fun decideRealizationType(
        categoryRealization: CategoryRealization,
        value: ExponenceValue,
        supplements: List<CategoryRandomSupplements>,
        speechPart: TypedSpeechPart,
        categories: List<SourcedCategory>,
        position: Int
    ): CategoryRealization {
        val categoryValues = value.categoryValues.map { it.categoryValue }
        val variants = supplements.map {
            it.specialRealization(categoryValues, speechPart.type, categories)
        }
        val finalVariants = uniteMutualProbabilities(variants) { copy(probability = it) }
            .filter { position == 0 || it.realization != NewWord }

        return finalVariants.randomUnwrappedElement()
            ?: categoryRealization
    }

    private fun splitCategoriesOnClusters(categories: List<SupplementedSourcedCategory>): List<ExponenceTemplate> {
        val shuffledCategories = categories.shuffled(RandomSingleton.random)
            .let {
                val nonCompulsory = it.filter { (c) -> !c.compulsoryData.isCompulsory }
                val compulsory = it.filter { (c) -> c.compulsoryData.isCompulsory }
                nonCompulsory + compulsory
            }
        val clusters = ArrayList<ExponenceTemplate>()
        var l = 0
        val data = mutableListOf<List<RealizationMapper>>()

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

            data.add(currentCategoriesWithSupplement.map { it.second::realizationTypeProbability })
            val mapper = { i: Int, c: CategoryRealization ->
                if (i == 0 || c != NewWord)
                    data[i].map { it(c) }.sum()
                else
                    0.0
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
    ): Pair<CategoryApplicator, Word?> = when (realizationType) {
        PrefixSeparateWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            PrefixWordCategoryApplicator(word) to word
        }
        SuffixSeparateWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            SuffixWordCategoryApplicator(word) to word
        }
        Prefix -> {
            val changes = changeGenerator.generateChanges(Position.Beginning, phoneticRestrictions)
            AffixCategoryApplicator(
                Prefix(changes),
                Prefix
            ) to null
        }
        Suffix -> {
            val change = changeGenerator.generateChanges(Position.End, phoneticRestrictions)
            AffixCategoryApplicator(
                Suffix(change),
                Suffix
            ) to null
        }
        Reduplication -> ReduplicationCategoryApplicator() to null
        Passing -> PassingCategoryApplicator to null
        NewWord -> {
            val word = lexisGenerator.generateWord(semanticsCore)
            NewWordCategoryApplicator(word) to word
        }
    }

    fun randomApplicatorsOrder(applicators: ApplicatorMap) = applicators.keys
            .sortedBy { k -> k.categories.joinToString { it.category.outType } }
            .shuffled(RandomSingleton.random)

    private fun constructExponenceUnionSets(
        categories: List<SupplementedSourcedCategory>,
        previousCategoryValues: List<CategoryValue> = listOf(),
        neighbourCategories: BoxedInt = BoxedInt(1)
    ): Set<List<SourcedCategoryValue>> = if (categories.size == 1)
        makeTrivialExponenceUnionSets(categories.first(), neighbourCategories, previousCategoryValues)
    else
        makeRecursiveExponenceUnionSets(categories, neighbourCategories, previousCategoryValues)

    private fun makeTrivialExponenceUnionSets(
        category: SupplementedSourcedCategory,
        neighbourCategories: BoxedInt,
        previousCategoryValues: List<CategoryValue>
    ) =
        if (neighbourCategories.value > 1 && testCollapse(category.second, previousCategoryValues)) {
            neighbourCategories.value--
            setOf(category.first.actualSourcedValues)
        } else
            category.first.actualSourcedValues.map { listOf(it) }.toSet()

    private fun makeRecursiveExponenceUnionSets(
        categories: List<SupplementedSourcedCategory>,
        neighbourCategories: BoxedInt,
        previousCategoryValues: List<CategoryValue>
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
        previousCategoryValues: List<CategoryValue>
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
        previousCategoryValues: List<CategoryValue>
    ): Set<List<SourcedCategoryValue>> {
        val lists = mutableSetOf<List<SourcedCategoryValue>>()

        for (newCategory in currentCategory.actualSourcedValues) {
            val recSets = constructExponenceUnionSets(
                categories,
                previousCategoryValues + listOf(newCategory.categoryValue),
                BoxedInt(neighbourCategories.value * currentCategory.actualSourcedValues.size)
            )
            lists.addAll(recSets.map { it + listOf(newCategory) })
        }

        return lists
    }

    private fun testCollapse(supplements: CategoryRandomSupplements, otherCategories: List<CategoryValue>): Boolean {
        val collapseProbability = categoryCollapseProbability /
                supplements.getCollapseCoefficient(otherCategories)

        return collapseProbability.testProbability()
    }
}

internal data class BoxedInt(var value: Int)

typealias RealizationMapper = (CategoryRealization) -> Double
typealias ApplicatorMap = Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>>

data class Result(val words: List<Word>, val solver: ApplicatorMap)

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
