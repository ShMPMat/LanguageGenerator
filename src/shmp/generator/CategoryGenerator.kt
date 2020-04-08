package shmp.generator

import shmp.language.*
import shmp.language.categories.*
import shmp.language.categories.realization.*
import shmp.language.morphem.*
import shmp.language.morphem.change.Position
import shmp.language.phonology.PhoneticRestrictions
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.random.Random

class CategoryGenerator(
    private val lexisGenerator: LexisGenerator,
    private val changeGenerator: ChangeGenerator,
    private val random: Random
) {
    private val categoryCollapseProbability = 0.5

    internal fun randomCategories() = listOf(
        randomArticles(),
        randomGender(),
        randomNumber()
    )

    private fun randomArticles(): Pair<Articles, (CategoryRealization) -> Double> {
        val presentElements = randomElement(
            ArticlePresence.values(),
            random
        ).presentArticles
        val affectedSpeechParts = randomAffectedSpeechParts(ArticlesRandomSupplements)
        return Articles(presentElements, affectedSpeechParts) to ArticlesRandomSupplements::realizationTypeProbability
    }

    private fun randomGender(): Pair<Gender, (CategoryRealization) -> Double> {
        val type = randomElement(
            GenderPresence.values(),
            random
        )
        val presentElements = if (type == GenderPresence.NonGendered)
            randomSublist(type.possibilities, random, min = 2)
        else
            type.possibilities
        val affectedSpeechParts = randomAffectedSpeechParts(GenderRandomSupplements)
        return Gender(presentElements, affectedSpeechParts) to GenderRandomSupplements::realizationTypeProbability
    }

    private fun randomNumber(): Pair<Numbers, (CategoryRealization) -> Double> {
        val presentElements = randomElement(
            NumbersPresence.values(),
            random
        ).presentNumbers
        val affectedSpeechParts = randomAffectedSpeechParts(NumbersRandomSupplements)
        return Numbers(presentElements, affectedSpeechParts) to NumbersRandomSupplements::realizationTypeProbability
    }

    private fun randomAffectedSpeechParts(categoryRandomSupplements: CategoryRandomSupplements): Set<SpeechPart> =
        setOf(categoryRandomSupplements.mainSpeechPart).union(
            randomSublist(
                SpeechPart.values(),
                categoryRandomSupplements::speechPartProbabilities,
                random
            )
        )

    internal fun randomApplicatorsForSpeechPart(
        speechPart: SpeechPart,
        phoneticRestrictions: PhoneticRestrictions,
        categoriesWithMappers: List<Pair<Category, (CategoryRealization) -> Double>>
    ): Map<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>> {
        val map = HashMap<ExponenceCluster, MutableMap<ExponenceValue, CategoryApplicator>>()
        val exponenceClustersWithMappers = splitCategoriesOnClusters(categoriesWithMappers)
        exponenceClustersWithMappers.forEach { map[it.first] = HashMap() }

        val realizationTypes = exponenceClustersWithMappers
            .map {
                it.first to randomElement(
                    CategoryRealization.values(),
                    it.second,
                    random
                )
            }
        realizationTypes.forEach { pair ->
            pair.first.possibleValues.forEach {
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
                (map[pair.first]
                    ?: throw GeneratorException("Couldn't put CategoryApplicator in map"))[it] =
                    randomCategoryApplicator(pair.second, phoneticRestrictions, syntaxCore)

            }
        }
        return map
    }

    private fun splitCategoriesOnClusters(
        categoriesWithMappers: List<Pair<Category, (CategoryRealization) -> Double>>
    ): List<Pair<ExponenceCluster, (CategoryRealization) -> Double>> {
        val shuffledMappers = categoriesWithMappers.shuffled(random)
        val clusters = ArrayList<Pair<ExponenceCluster, (CategoryRealization) -> Double>>()
        var l = 0
        while (l < shuffledMappers.size) {
            val r = randomElement(l + 1..shuffledMappers.size, { 1.0 / it }, random)
            val categories = shuffledMappers.subList(l, r).map { it.first }
            val cluster = ExponenceCluster(
                categories,
                constructExponenceUnionSets(
                    categories
                )
            )
            val mapper = shuffledMappers[l].second //TODO how to unite a few lambdas, help
            clusters.add(cluster to mapper)
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
            }
            else
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