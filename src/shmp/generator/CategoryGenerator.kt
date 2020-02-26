package shmp.generator

import shmp.language.*
import shmp.language.categories.*
import shmp.language.categories.realization.AffixCategoryApplicator
import shmp.language.categories.realization.CategoryApplicator
import shmp.language.categories.realization.PrefixWordCategoryApplicator
import shmp.language.categories.realization.SuffixWordCategoryApplicator
import shmp.language.morphem.*
import shmp.language.morphem.change.Position
import shmp.language.phonology.PhoneticRestrictions
import shmp.random.randomElementWithProbability
import shmp.random.randomSublistWithProbability
import kotlin.random.Random

class CategoryGenerator(
    private val lexisGenerator: LexisGenerator,
    private val changeGenerator: ChangeGenerator,
    private val random: Random
) {
    internal fun randomCategories() = listOf(
        randomArticles(),
        randomGender(),
        randomNumber()
    )

    private fun randomArticles(): Pair<Articles, (CategoryRealization) -> Double> {
        val presentElements = randomElementWithProbability(
            ArticlePresence.values(),
            random
        ).presentArticles
        val affectedSpeechParts = generateAffectedSpeechParts(ArticlesRandomSupplements)
        return Articles(presentElements, affectedSpeechParts) to ArticlesRandomSupplements::realizationTypeProbability
    }

    private fun randomGender(): Pair<Gender, (CategoryRealization) -> Double> {
        val presentElements = randomElementWithProbability(
            GenderPresence.values(),
            random
        ).possibilities
        val affectedSpeechParts = generateAffectedSpeechParts(GenderRandomSupplements)
        return Gender(presentElements, affectedSpeechParts) to GenderRandomSupplements::realizationTypeProbability
    }

    private fun randomNumber(): Pair<Numbers, (CategoryRealization) -> Double> {
        val presentElements = randomElementWithProbability(
            NumbersPresence.values(),
            random
        ).presentNumbers
        val affectedSpeechParts = generateAffectedSpeechParts(NumbersRandomSupplements)
        return Numbers(presentElements, affectedSpeechParts) to NumbersRandomSupplements::realizationTypeProbability
    }

    private fun generateAffectedSpeechParts(categoryRandomSupplements: CategoryRandomSupplements): Set<SpeechPart> =
        setOf(categoryRandomSupplements.mainSpeechPart).union(
            randomSublistWithProbability(
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
                it.first to randomElementWithProbability(
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
            val r = randomElementWithProbability(l + 1..shuffledMappers.size, { 1.0 / it }, random)
            val cluster = ExponenceCluster(
                shuffledMappers.subList(
                    l,
                    r
                ).map { it.first })
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
    }

    fun randomApplicatorsOrder(
        applicators: Map<ExponenceCluster,
                Map<ExponenceValue, CategoryApplicator>>
    ): List<ExponenceCluster> = applicators.keys.shuffled(random)
}
