package shmp.generator

import shmp.language.*
import shmp.language.categories.*
import shmp.language.categories.realization_type.AffixCategoryApplicator
import shmp.language.categories.realization_type.CategoryApplicator
import shmp.language.categories.realization_type.PrefixWordCategoryApplicator
import shmp.language.categories.realization_type.SuffixWordCategoryApplicator
import shmp.language.morphem.*
import shmp.random.SampleSpaceObject
import shmp.random.randomElementWithProbability
import shmp.random.randomSublistWithProbability
import kotlin.random.Random

class CategoryGenerator(
    private val lexisGenerator: LexisGenerator,
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
        val affectedSpeechParts = generateAffectedSpeechParts(GenderRandomSupplements)
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
                    randomCategoryApplicator(pair.second, syntaxCore)

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
            val changes = generateChanges(Position.Beginning, false)
            AffixCategoryApplicator(
                Prefix(TemplateWordChange(changes.map { TemplateChange(Position.Beginning, it.first, it.second) })),
                CategoryRealization.Prefix
            )
        }
        CategoryRealization.Suffix -> {
            val changes = generateChanges(Position.End, true)
            AffixCategoryApplicator(
                Suffix(TemplateWordChange(changes.map { TemplateChange(Position.End, it.first, it.second) })),
                CategoryRealization.Suffix
            )
        }
    }

    private fun generateChanges(
        position: Position,
        isClosed: Boolean
    ): List<Pair<List<PositionTemplate>, List<PositionSubstitution>>> {
        val getSyllableSubstitution = { c: Boolean, i: Boolean ->
            lexisGenerator.syllableGenerator.generateSyllable(
                lexisGenerator.phonemeContainer,
                random,
                canHaveFinal = c,
                shouldHaveInitial = i
            ).phonemeSequence.phonemes
                .map { PhonemePositionSubstitution(it) }
        }
        val result = when (randomElementWithProbability(
            AffixTypes.values(),
            random
        )) {
            AffixTypes.UniversalAffix -> {
                listOf(
                    listOf<PositionTemplate>() to getSyllableSubstitution(isClosed, false)
                )
            }
            AffixTypes.PhonemeTypeAffix -> {
                val addPasser = { list: List<PositionSubstitution>, sub: PositionSubstitution ->
                    when (position) {
                        Position.Beginning -> list + listOf(sub)
                        Position.End -> listOf(sub) + list
                    }
                }
                PhonemeType.values().map {
                    listOf(TypePositionTemplate(it)) to addPasser(
                        getSyllableSubstitution(
                            isClosed || it == PhonemeType.Vowel,
                            it == PhonemeType.Vowel && position == Position.End
                        ),
                        PassingPositionSubstitution()
                    )
                }
            }
        }
        return result
    }
}

enum class AffixTypes(override val probability: Double) : SampleSpaceObject {
    UniversalAffix(100.0),
    PhonemeTypeAffix(50.0)
}