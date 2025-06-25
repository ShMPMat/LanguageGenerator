package io.tashtabash.lang.generator

import io.tashtabash.lang.generator.util.SyllablePosition
import io.tashtabash.lang.generator.util.SyllableRestrictions
import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.morphem.change.*
import io.tashtabash.lang.language.morphem.change.substitution.EpenthesisSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PassingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.ExactPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.phonology.matcher.*
import io.tashtabash.lang.language.syntax.ChangeParadigm
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.randomElement


class ChangeGenerator(val lexisGenerator: LexisGenerator) {
    private val generationAttempts = 10

    internal fun generateChanges(position: Position, restrictions: PhoneticRestrictions): TemplateChange {
        val (hasInitial, hasFinal) = when (position) {
            Position.Beginning -> null to true
            Position.End -> true to null
        }
        val rawSubstitutions: List<TemplateChange> = when (AffixTypes.entries.randomElement()) {
            AffixTypes.UniversalAffix -> {
                val affix = generateSyllableAffix(restrictions, hasInitial, hasFinal)
                val templates = listOf(
                    GeneratedChange(position, listOf(), listOf(), affix)
                )

                eliminateCollisionsByEpenthesis(templates, restrictions)
            }
            AffixTypes.PhonemeTypeAffix -> {
                val templates = PhonemeType.entries.map {
                    val affix = generateSyllableAffix(
                        restrictions,
                        hasInitial == true || it == PhonemeType.Vowel,
                        it == PhonemeType.Vowel && position == Position.End
                    )
                    val substitutions = listOf(PassingPhonemeSubstitution)

                    GeneratedChange(position, listOf(TypePhonemeMatcher(it)), substitutions, affix)
                }
                randomCollisionElimination(templates, restrictions)
            }
        }
        return createSimplifiedTemplateChange(rawSubstitutions)
    }

    private fun eliminateCollisionsByEpenthesis(
        templateChanges: List<GeneratedChange>,
        restrictions: PhoneticRestrictions
    ): List<TemplateChange> {
        return templateChanges.flatMap { change ->
            if (change.affix.size == 1)
                return@flatMap randomCollisionElimination(templateChanges, restrictions)

            val isBeginning = change.position == Position.Beginning
            val vowelAdjacentAffix = if (isBeginning)
                change.affix.dropLastWhile { it.exactPhoneme.type == PhonemeType.Vowel }
            else
                change.affix.dropWhile { it.exactPhoneme.type == PhonemeType.Vowel }

            listOf(
                change.copy(
                    phonemeMatchers = listOf(TypePhonemeMatcher(PhonemeType.Consonant)),
                    matchedPhonemesSubstitution = listOf(PassingPhonemeSubstitution),
                ).toTemplateSingleChange(),
                change.copy(
                    phonemeMatchers = listOf(TypePhonemeMatcher(PhonemeType.Vowel)),
                    matchedPhonemesSubstitution = listOf(PassingPhonemeSubstitution),
                    affix = vowelAdjacentAffix
                ).toTemplateSingleChange(),
            )
        }
    }

    private fun randomCollisionElimination(
        templateChanges: List<GeneratedChange>,
        restrictions: PhoneticRestrictions
    ): List<TemplateChange> {
        return templateChanges.map { change ->
            var result: TemplateChange = change.toTemplateSingleChange()
            val borderPhoneme = getBorderPhoneme(change)
            val borderAffixMatcher = when (change.position) {
                Position.Beginning -> change.phonemeMatchers.firstOrNull()
                Position.End -> change.phonemeMatchers.lastOrNull()
            } ?: PassingPhonemeMatcher
            val hasCollision = when (change.position) {
                Position.Beginning -> restrictions.initialWordPhonemes
                Position.End -> restrictions.finalWordPhonemes
            }.filter { phoneme -> borderAffixMatcher.match(phoneme) }
                .any { phoneme -> doPhonemesCollide(phoneme, borderPhoneme) }

            if (hasCollision)
                result = removeCollision(change, restrictions, borderPhoneme) ?: result

            result
        }
    }

    private fun removeCollision(
        wordChange: GeneratedChange,
        restrictions: PhoneticRestrictions,
        borderPhoneme: Phoneme
    ): TemplateChange? {
        for (i in 1..generationAttempts) {
            val newChange =
                generateSyllableAffix(restrictions, null, null)
            val newBorderPhoneme = when (wordChange.position) {
                Position.Beginning -> newChange.last()
                Position.End -> newChange[0]
            }.exactPhoneme
            if (!doPhonemesCollide(newBorderPhoneme, borderPhoneme)) {
                return createSimplifiedTemplateChange(listOf(
                    makeTemplateChangeWithBorderPhoneme(wordChange, newChange, borderPhoneme).toTemplateSingleChange(),
                    wordChange.toTemplateSingleChange()
                ))
            }
        }
        return null
    }

    private fun makeTemplateChangeWithBorderPhoneme(
        oldChange: GeneratedChange,
        newAffix: List<ExactPhonemeSubstitution>,
        neededPhoneme: Phoneme
    ): GeneratedChange {
        val singleMatcher = listOf(ExactPhonemeMatcher(neededPhoneme))
        val singleSubstitution = listOf(PassingPhonemeSubstitution)
        var phonemeMatcher = oldChange.phonemeMatchers
        var matchedPhonemeSubstitution = oldChange.matchedPhonemesSubstitution

        phonemeMatcher = if (phonemeMatcher.size > 1) when (oldChange.position) {
            Position.Beginning -> singleMatcher + phonemeMatcher.drop(1)
            Position.End -> phonemeMatcher.dropLast(1) + singleMatcher
        } else singleMatcher
        matchedPhonemeSubstitution = if (matchedPhonemeSubstitution.size > 1) when (oldChange.position) {
            Position.Beginning -> singleSubstitution + matchedPhonemeSubstitution.drop(1)
            Position.End -> matchedPhonemeSubstitution.dropLast(1) + singleSubstitution
        } else singleSubstitution

        return GeneratedChange(oldChange.position, phonemeMatcher, matchedPhonemeSubstitution, newAffix)
    }

    private fun getBorderPhoneme(singleChange: GeneratedChange) = when (singleChange.position) {
        Position.Beginning -> singleChange.affix.last()
        Position.End -> singleChange.affix.first()
    }.exactPhoneme

    private fun generateSyllableAffix(
        phoneticRestrictions: PhoneticRestrictions,
        hasInitial: Boolean?,
        hasFinal: Boolean?
    ) = lexisGenerator.syllableGenerator.generateSyllable(
        SyllableRestrictions(
            lexisGenerator.phonemeContainer,
            phoneticRestrictions,
            if (hasFinal == true) SyllablePosition.End else SyllablePosition.Middle,
            hasInitial = hasInitial,
            hasFinal = hasFinal
        )
    ).phonemes.phonemes
        .map { ExactPhonemeSubstitution(it) }


    fun injectIrregularity(paradigm: ChangeParadigm, lexis: Lexis): ChangeParadigm {
        val newChangeParadigms = mutableMapOf<TypedSpeechPart, SpeechPartChangeParadigm>()

        for (speechPart in paradigm.wordChangeParadigm.speechParts) {
            val speechPartParadigm = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart)
            val newApplicators = speechPartParadigm.orderedApplicators
                .mapIndexed { _, (c, m) ->
                    c to ValueMap(m)
                }.toMap()

            newChangeParadigms[speechPart] = speechPartParadigm.copy(applicators = newApplicators)
        }

        return paradigm.copy(
            wordChangeParadigm = paradigm.wordChangeParadigm.copy(speechPartChangeParadigms = newChangeParadigms)
        )
    }
}

data class GeneratedChange(
    val position: Position,
    val phonemeMatchers: List<PhonemeMatcher>,
    val matchedPhonemesSubstitution: List<PhonemeSubstitution>,
    val affix: List<ExactPhonemeSubstitution>
) {
    fun toTemplateSingleChange(): TemplateSingleChange {
        val stemPassingMatcher =
            if (matchedPhonemesSubstitution.isEmpty())
                listOf(PassingPhonemeMatcher)
            else listOf()

        return TemplateSingleChange(
            position,
            PhonologicalRule(
                if (position == Position.Beginning)
                    listOf(BorderPhonemeMatcher) + phonemeMatchers + stemPassingMatcher
                else
                    stemPassingMatcher + phonemeMatchers + listOf(BorderPhonemeMatcher),
                if (position == Position.Beginning) 1 else stemPassingMatcher.size,
                if (position == Position.Beginning) stemPassingMatcher.size else 1,
                if (position == Position.Beginning)
                    affix.map { EpenthesisSubstitution(it.exactPhoneme) } + matchedPhonemesSubstitution
                else
                    matchedPhonemesSubstitution + affix.map { EpenthesisSubstitution(it.exactPhoneme) }
            ).trim()
        )
    }
}


enum class AffixTypes(override val probability: Double) : SampleSpaceObject {
    UniversalAffix(100.0),
    PhonemeTypeAffix(50.0)
}
