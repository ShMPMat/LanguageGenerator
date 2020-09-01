package shmp.generator

import shmp.language.PhonemeType
import shmp.language.morphem.change.*
import shmp.language.phonology.Phoneme
import shmp.language.phonology.PhoneticRestrictions
import shmp.language.phonology.doesPhonemesCollide
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random


class ChangeGenerator(
    val lexisGenerator: LexisGenerator,
    private val random: Random
) {
    private val generationAttempts = 10

    internal fun generateChanges(
        position: Position,
        restrictions: PhoneticRestrictions
    ): TemplateSequenceChange {
        val isClosed = when (position) {
            Position.Beginning -> false
            Position.End -> true
        }
        val rawSubstitutions = when (randomElement(AffixTypes.values(), random)) {
            AffixTypes.UniversalAffix -> {
                val templates = listOf(
                    TemplateSingleChange(
                        position,
                        listOf(),
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
                        listOf(PassingPositionSubstitution()),
                        generateSyllableAffix(
                            restrictions,
                            isClosed || it == PhonemeType.Vowel,
                            it == PhonemeType.Vowel && position == Position.End
                        )
                    )
                }
                randomDoubleEdgeLettersElimination(templates, restrictions)
            }
        }
        return TemplateSequenceChange(rawSubstitutions)
    }

    private fun randomDoubleEdgeLettersElimination(
        templateChanges: List<TemplateSingleChange>,
        restrictions: PhoneticRestrictions
    ): List<WordChange> {
        return templateChanges
            .map {
                var result: WordChange = it
                val borderPhoneme = getBorderPhoneme(it) ?: return@map result
                val borderAffixMatcher = when (it.position) {
                    Position.Beginning -> it.phonemeMatchers.firstOrNull()
                    Position.End -> it.phonemeMatchers.lastOrNull()
                } ?: PassingMatcher()
                val hasCollision = when (it.position) {
                    Position.Beginning -> restrictions.initialWordPhonemes
                    Position.End -> restrictions.finalWordPhonemes
                }.filter { phoneme -> borderAffixMatcher.test(phoneme) }
                    .any { phoneme -> doesPhonemesCollide(phoneme, borderPhoneme) }
                if (hasCollision) {
                    result = removeCollision(it, restrictions, borderPhoneme) ?: result
                }
                result
            }
    }

    private fun removeCollision(
        wordChange: TemplateSingleChange,
        restrictions: PhoneticRestrictions,
        borderPhoneme: Phoneme
    ): WordChange? {
        for (i in 1..generationAttempts) {
            val newChange =
                generateSyllableAffix(restrictions, canHaveFinal = true, shouldHaveFinal = false)
            val newBorderPhoneme = when (wordChange.position) {
                Position.Beginning -> newChange.last()
                Position.End -> newChange[0]
            }.phoneme
            if (!doesPhonemesCollide(newBorderPhoneme, borderPhoneme)) {
                return TemplateSequenceChange(
                    makeTemplateChangeWithBorderPhoneme(
                        wordChange,
                        newChange,
                        borderPhoneme
                    ),
                    wordChange
                )
            }
        }
        return null
    }

    private fun makeTemplateChangeWithBorderPhoneme(
        oldChange: TemplateSingleChange,
        newAffix: List<PositionSubstitution>,
        neededPhoneme: Phoneme
    ): TemplateSingleChange {
        val singleMatcher = listOf(PhonemeMatcher(neededPhoneme))
        val singleSubstitution = listOf(PassingPositionSubstitution())
        var phonemeMatcher = oldChange.phonemeMatchers
        var matchedPhonemeSubstitution = oldChange.matchedPhonemesSubstitution
        phonemeMatcher = if (phonemeMatcher.size <= 1) {
            singleMatcher
        } else when (oldChange.position) {
            Position.Beginning -> singleMatcher + phonemeMatcher.drop(1)
            Position.End -> phonemeMatcher.dropLast(1) + singleMatcher
        }
        matchedPhonemeSubstitution = if (matchedPhonemeSubstitution.size <= 1) {
            singleSubstitution
        } else when (oldChange.position) {
            Position.Beginning -> singleSubstitution + matchedPhonemeSubstitution.drop(1)
            Position.End -> matchedPhonemeSubstitution.dropLast(1) + singleSubstitution
        }
        return TemplateSingleChange(
            oldChange.position,
            phonemeMatcher,
            matchedPhonemeSubstitution,
            newAffix
        )
    }

    private fun getBorderPhoneme(singleChange: TemplateSingleChange): Phoneme? = when (singleChange.position) {
        Position.Beginning -> singleChange.affix.last()
            .getSubstitutePhoneme()
        Position.End -> singleChange.affix[0]
            .getSubstitutePhoneme()
    }

    private fun generateSyllableAffix(
        phoneticRestrictions: PhoneticRestrictions,
        canHaveFinal: Boolean,
        shouldHaveFinal: Boolean
    ) = lexisGenerator.syllableGenerator.generateSyllable(
        SyllableRestrictions(
            lexisGenerator.phonemeContainer,
            phoneticRestrictions,
            if (canHaveFinal) SyllablePosition.End else SyllablePosition.Middle,
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
