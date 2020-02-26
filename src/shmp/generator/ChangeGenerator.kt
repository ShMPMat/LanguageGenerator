package shmp.generator

import shmp.language.PhonemeType
import shmp.language.morphem.change.*
import shmp.language.phonology.PhoneticRestrictions
import shmp.random.SampleSpaceObject
import shmp.random.randomElementWithProbability
import kotlin.random.Random

class ChangeGenerator(
    private val lexisGenerator: LexisGenerator,
    private val random: Random
) {
    internal fun generateChanges(
        position: Position,
        phoneticRestrictions: PhoneticRestrictions
    ): TemplateSequenceChange {
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
                TemplateSingleChange(
                    position,
                    listOf(),
                    generateSyllableAffix(phoneticRestrictions, isClosed, false)
                )
            )
            AffixTypes.PhonemeTypeAffix -> {
                val addPasser = { list: List<PositionSubstitution>, sub: PositionSubstitution ->
                    when (position) {
                        Position.Beginning -> list + listOf(sub)
                        Position.End -> listOf(sub) + list
                    }
                }
                PhonemeType.values().map {
                    TemplateSingleChange(
                        position,
                        listOf(TypePositionMatcher(it)),
                        addPasser(
                            generateSyllableAffix(
                                phoneticRestrictions,
                                isClosed || it == PhonemeType.Vowel,
                                it == PhonemeType.Vowel && position == Position.End
                            ),
                            PassingPositionSubstitution()
                        )
                    )
                }
            }
        }
        return TemplateSequenceChange(rawSubstitutions)
    }

    private fun generateSyllableAffix(
        phoneticRestrictions: PhoneticRestrictions,
        canHaveFinal: Boolean,
        shouldHaveFinal: Boolean
    ) =
        lexisGenerator.syllableGenerator.generateSyllable(
            SyllableRestrictions(
                lexisGenerator.phonemeContainer,
                phoneticRestrictions,
                canHaveFinal = canHaveFinal,
                shouldHaveInitial = shouldHaveFinal
            ),
            random
        ).phonemeSequence.phonemes
            .map { PhonemePositionSubstitution(it) }
}

enum class AffixTypes(override val probability: Double) : SampleSpaceObject {
    UniversalAffix(100.0),
    PhonemeTypeAffix(50.0)
}