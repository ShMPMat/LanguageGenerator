package shmp.generator

import shmp.language.PhonemeType
import shmp.language.morphem.change.*
import shmp.language.phonology.Phoneme
import shmp.language.phonology.PhonemeSequence
import shmp.language.phonology.PhoneticRestrictions
import shmp.random.SampleSpaceObject
import shmp.random.randomElementWithProbability
import kotlin.random.Random

class ChangeGenerator(
    private val lexisGenerator: LexisGenerator,
    private val random: Random
) {
    val GENERATION_ATTEMPTS = 10

    internal fun generateChanges(
        position: Position,
        restrictions: PhoneticRestrictions
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
            AffixTypes.UniversalAffix -> {
                val templates = listOf(
                    TemplateSingleChange(
                        position,
                        listOf(),
                        generateSyllableAffix(restrictions, isClosed, false)
                    )
                )
                randomDoubleEdgeLettersElimination(templates, restrictions)
            }
            AffixTypes.PhonemeTypeAffix -> {
                val templates = PhonemeType.values().map {
                    TemplateSingleChange(
                        position,
                        listOf(TypePositionMatcher(it)),
                        addPasser(
                            position,
                            generateSyllableAffix(
                                restrictions,
                                isClosed || it == PhonemeType.Vowel,
                                it == PhonemeType.Vowel && position == Position.End
                            ),
                            listOf(PassingPositionSubstitution())
                        )
                    )
                }
                randomDoubleEdgeLettersElimination(templates, restrictions)
            }
        }
        return TemplateSequenceChange(rawSubstitutions)
    }

    private fun addPasser(position: Position, affix: List<PositionSubstitution>, change: List<PositionSubstitution>) =
        when (position) {
            Position.Beginning -> affix + change
            Position.End -> change + affix
        }

    private fun randomDoubleEdgeLettersElimination(
        templateChanges: List<TemplateSingleChange>,
        restrictions: PhoneticRestrictions
    ): List<WordChange> {
        val processedChanges = mutableListOf<WordChange>()
        templateChanges.forEach {
            var result: WordChange = it
            val borderPhoneme = getBorderPhoneme(it) ?: return@forEach
            val hasCollision = when (it.position) {
                Position.Beginning -> restrictions.initialWordPhonemes
                Position.End -> restrictions.finalWordPhonemes
            }.contains(PhonemeSequence(borderPhoneme))
            if (hasCollision) {
                for (i in 1..GENERATION_ATTEMPTS) {
                    val newChange = generateSyllableAffix(restrictions, canHaveFinal = true, shouldHaveFinal = false)
                    val newBorderPhoneme = when (it.position) {
                        Position.Beginning -> newChange.last()
                        Position.End -> newChange[0]
                    }.phoneme
                    if (newBorderPhoneme != borderPhoneme) {
                        val sub = when (it.position) {
                            Position.Beginning -> it.result.subList(it.phonemes.size, it.result.size)
                            Position.End -> it.result.subList(0, it.phonemes.size)
                        }
                        result = TemplateSequenceChange(
                            TemplateSingleChange(it.position, it.phonemes, addPasser(it.position, newChange, sub)), //TODO wont work, it will always be first
                            it
                        )
                        break
                    }
                }
            }
            processedChanges.add(result)
        }
        return processedChanges
    }

    private fun getBorderPhoneme(singleChange: TemplateSingleChange): Phoneme? = when (singleChange.position) {
        Position.Beginning -> singleChange.result[singleChange.result.lastIndex - singleChange.phonemes.size]
            .getSubstitutePhoneme()
        Position.End -> singleChange.result[singleChange.phonemes.size]
            .getSubstitutePhoneme()
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