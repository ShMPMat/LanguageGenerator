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
    internal fun randomArticles(): Pair<Articles, Map<SpeechPart, Map<CategoryEnum, CategoryApplicator>>> {
        val presentElements = randomElementWithProbability(
            ArticlePresence.values(),
            random
        ).presentArticles
        val applicators = randomCategoryApplicators(
            presentElements.toSet(),
            CategoryRealization::probabilityForArticle,
            setOf(SpeechPart.Noun).union(
                randomSublistWithProbability(
                    SpeechPart.values(),
                    SpeechPart::probabilityForArticle,
                    random
                )
            ).toList()
        )
        return Articles(presentElements) to applicators
    }

    internal fun randomGender(): Pair<Gender, Map<SpeechPart, Map<CategoryEnum, CategoryApplicator>>> {
        val presentElements = randomElementWithProbability(
            GenderPresence.values(),
            { it.probability },
            random
        ).possibilities
        val applicators = randomCategoryApplicators(
            presentElements.toSet(),
            CategoryRealization::probabilityForGender,
            setOf(SpeechPart.Noun).union(
                randomSublistWithProbability(
                    SpeechPart.values(),
                    SpeechPart::probabilityForGender,
                    random
                )
            ).toList()
        )
        return Gender(presentElements) to applicators
    }

    private fun randomCategoryApplicators(
        presentElements: Set<CategoryEnum>,
        mapper: (CategoryRealization) -> Double,
        speechParts: List<SpeechPart>
    ): Map<SpeechPart, Map<CategoryEnum, CategoryApplicator>> {

        val map = HashMap<SpeechPart, Map<CategoryEnum, CategoryApplicator>>()
        for (speechPart in speechParts) {
            val mapForSpeechPart = HashMap<CategoryEnum, CategoryApplicator>()
            val realizationType = randomElementWithProbability(
                CategoryRealization.values(),
                mapper,
                random
            )
            presentElements.forEach {
                mapForSpeechPart[it] =
                    randomCategoryApplicator(realizationType, it.syntaxCore)
            }
            map[speechPart] = mapForSpeechPart
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
            lexisGenerator.syllableTemplate.generateSyllable(
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