package shmp.generator

import shmp.language.CategoryRealization
import shmp.language.PhonemeType
import shmp.language.categories.realization.AffixCategoryApplicator
import shmp.language.morphem.*
import shmp.random.SampleSpaceObject
import shmp.random.randomElementWithProbability
import kotlin.random.Random

class ChangeGenerator(
    private val lexisGenerator: LexisGenerator,
    private val random: Random
) {
    internal fun generateChanges(
        position: Position,
        isClosed: Boolean
    ): TemplateWordChange {
        val getSyllableSubstitution = { c: Boolean, i: Boolean ->
            lexisGenerator.syllableGenerator.generateSyllable(
                lexisGenerator.phonemeContainer,
                random,
                canHaveFinal = c,
                shouldHaveInitial = i
            ).phonemeSequence.phonemes
                .map { PhonemePositionSubstitution(it) }
        }
        val substitutionList = when (randomElementWithProbability(
            AffixTypes.values(),
            random
        )) {
            AffixTypes.UniversalAffix -> {
                listOf(
                    listOf<PositionMatcher>() to getSyllableSubstitution(isClosed, false)
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
                    listOf(TypePositionMatcher(it)) to addPasser(
                        getSyllableSubstitution(
                            isClosed || it == PhonemeType.Vowel,
                            it == PhonemeType.Vowel && position == Position.End
                        ),
                        PassingPositionSubstitution()
                    )
                }
            }
        }
        val result = when (position) {
            Position.Beginning ->
                TemplateWordChange(substitutionList
                    .map {
                        TemplateChange(
                            Position.Beginning,
                            it.first,
                            it.second
                        )
                    })
            Position.End -> TemplateWordChange(substitutionList
                .map {
                    TemplateChange(
                        Position.End,
                        it.first,
                        it.second
                    )
                })
        }
        return result
    }
}

enum class AffixTypes(override val probability: Double) : SampleSpaceObject {
    UniversalAffix(100.0),
    PhonemeTypeAffix(50.0)
}