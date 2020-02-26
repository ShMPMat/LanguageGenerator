package shmp.generator

import shmp.language.PhonemeType
import shmp.language.morphem.*
import shmp.language.phonology.Restrictions
import shmp.random.SampleSpaceObject
import shmp.random.randomElementWithProbability
import kotlin.random.Random

class ChangeGenerator(
    private val lexisGenerator: LexisGenerator,
    private val random: Random
) {
    internal fun generateChanges(
        position: Position,
        restrictions: Restrictions
    ): TemplateWordChange {
        val isClosed = when (position) {
            Position.Beginning -> false
            Position.End -> true
        }
        val rawSubstitutions = when (
            randomElementWithProbability(
                AffixTypes.values(),
                random
            )) {
            AffixTypes.UniversalAffix -> listOf(
                TemplateChange(position, listOf(), generateSyllableAffix(isClosed, false))
            )
            AffixTypes.PhonemeTypeAffix -> {
                val addPasser = { list: List<PositionSubstitution>, sub: PositionSubstitution ->
                    when (position) {
                        Position.Beginning -> list + listOf(sub)
                        Position.End -> listOf(sub) + list
                    }
                }
                PhonemeType.values().map {
                    TemplateChange(
                        position,
                        listOf(TypePositionMatcher(it)),
                        addPasser(
                            generateSyllableAffix(
                                isClosed || it == PhonemeType.Vowel,
                                it == PhonemeType.Vowel && position == Position.End
                            ),
                            PassingPositionSubstitution()
                        )
                    )
                }
            }
        }
        return TemplateWordChange(rawSubstitutions)
    }

    private fun generateSyllableAffix(canHaveFinal: Boolean, shouldHaveFinal: Boolean) =
        lexisGenerator.syllableGenerator.generateSyllable(
            lexisGenerator.phonemeContainer,
            random,
            canHaveFinal = canHaveFinal,
            shouldHaveInitial = shouldHaveFinal
        ).phonemeSequence.phonemes
            .map { PhonemePositionSubstitution(it) }
}

enum class AffixTypes(override val probability: Double) : SampleSpaceObject {
    UniversalAffix(100.0),
    PhonemeTypeAffix(50.0)
}