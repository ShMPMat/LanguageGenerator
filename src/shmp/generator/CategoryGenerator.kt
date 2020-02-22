package shmp.generator

import shmp.language.*
import shmp.language.categories.*
import shmp.language.categories.change.AffixCategoryApplicator
import shmp.language.categories.change.CategoryApplicator
import shmp.language.categories.change.PrefixWordCategoryApplicator
import shmp.language.categories.change.SuffixWordCategoryApplicator
import shmp.language.morphem.*
import shmp.random.SampleSpaceObject
import shmp.random.randomElementWithProbability
import shmp.random.randomSublistWithProbability
import kotlin.random.Random

class CategoryGenerator(
    private val lexisGenerator: LexisGenerator,
    private val random: Random
) {
    internal fun randomArticles(): Pair<Articles, (CategoryRealization) -> Double> {
        val presentElements = randomElementWithProbability(
            ArticlePresence.values(),
            random
        ).presentArticles
//        val applicators = randomCategoryApplicators(
//            presentElements.toSet(),
//            CategoryRealization::probabilityForArticle,
//            setOf(SpeechPart.Noun).union(
//                randomSublistWithProbability(
//                    SpeechPart.values(),
//                    SpeechPart::probabilityForArticle,
//                    random
//                )
//            ).toList()
//        )
        return Articles(
            presentElements,
            setOf(SpeechPart.Noun).union(
                randomSublistWithProbability(
                    SpeechPart.values(),
                    SpeechPart::probabilityForArticle,
                    random
                )
            )
        ) to CategoryRealization::probabilityForArticle
    }

    internal fun randomGender(): Pair<Gender, (CategoryRealization) -> Double> {
        val presentElements = randomElementWithProbability(
            GenderPresence.values(),
            { it.probability },
            random
        ).possibilities
//        val applicators = randomCategoryApplicators(
//            presentElements.toSet(),
//            CategoryRealization::probabilityForGender,
//            setOf(SpeechPart.Noun).union(
//                randomSublistWithProbability(
//                    SpeechPart.values(),
//                    SpeechPart::probabilityForGender,
//                    random
//                )
//            ).toList()
//        )
        return Gender(
            presentElements,
            setOf(SpeechPart.Noun).union(
                randomSublistWithProbability(
                    SpeechPart.values(),
                    SpeechPart::probabilityForGender,
                    random
                )
            )
        ) to CategoryRealization::probabilityForGender
    }

    internal fun randomApplicatorsForSpeechPart(
        speechPart: SpeechPart,
        categoriesWithMappers: List<Pair<Category, (CategoryRealization) -> Double>>
    ): Map<ExponenceCluster, Map<ExponenceUnion, CategoryApplicator>> {
        val map = HashMap<ExponenceCluster, MutableMap<ExponenceUnion, CategoryApplicator>>()
        val exponenceClustersWithMappers = categoriesWithMappers
            .map { ExponenceCluster(listOf(it.first)) to it.second }
        val realizationTypes = exponenceClustersWithMappers
            .map {
                it.first to randomElementWithProbability(
                    CategoryRealization.values(),
                    it.second,
                    random
                )
            }
        exponenceClustersWithMappers.forEach { map[it.first] = HashMap() }
        realizationTypes.forEach { pair ->
            pair.first.possibleCategories.forEach {
                var syntaxCore = it.categoryEnums[0].syntaxCore
                for (core in it.categoryEnums.subList(1, it.categoryEnums.size).map { it.syntaxCore }) {
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