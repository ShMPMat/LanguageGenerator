package io.tashtabash.lang.generator

import io.tashtabash.lang.generator.util.SyllablePosition
import io.tashtabash.lang.generator.util.SyllableRestrictions
import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.change.*
import io.tashtabash.lang.language.morphem.change.matcher.PassingMatcher
import io.tashtabash.lang.language.morphem.change.matcher.PhonemeMatcher
import io.tashtabash.lang.language.morphem.change.matcher.TypePositionMatcher
import io.tashtabash.lang.language.morphem.change.substitution.PassingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.ExactPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.syntax.ChangeParadigm
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement


class ChangeGenerator(val lexisGenerator: LexisGenerator) {
    private val generationAttempts = 10

    internal fun generateChanges(position: Position, restrictions: PhoneticRestrictions): TemplateSequenceChange {
        val (hasInitial, hasFinal) = when (position) {
            Position.Beginning -> null to true
            Position.End -> true to null
        }
        val rawSubstitutions = when (AffixTypes.values().randomElement()) {
            AffixTypes.UniversalAffix -> {
                val affix = generateSyllableAffix(restrictions, hasInitial, hasFinal)
                val templates = listOf(
                    TemplateSingleChange(position, listOf(), listOf(), affix)
                )

                eliminateCollisionsByEpenthesis(templates, restrictions)
            }
            AffixTypes.PhonemeTypeAffix -> {
                val templates = PhonemeType.values().map {
                    val affix = generateSyllableAffix(
                        restrictions,
                        hasInitial == true || it == PhonemeType.Vowel,
                        it == PhonemeType.Vowel && position == Position.End
                    )
                    val substitutions = listOf(PassingPhonemeSubstitution())
                    val isBeginning = position == Position.Beginning

                    TemplateSingleChange(position, listOf(TypePositionMatcher(it, isBeginning)), substitutions, affix)
                }
                randomCollisionElimination(templates, restrictions)
            }
        }
        return TemplateSequenceChange(rawSubstitutions)
    }

    private fun eliminateCollisionsByEpenthesis(
        templateChanges: List<TemplateSingleChange>,
        restrictions: PhoneticRestrictions
    ): List<WordChange> {
        return templateChanges.flatMap { change ->
            if (change.affix.size == 1)
                return@flatMap randomCollisionElimination(templateChanges, restrictions)

            val isBeginning = change.position == Position.Beginning
            val vowelAdjacentAffix = if (isBeginning)
                change.affix.dropLastWhile { it.getSubstitutePhoneme()?.type == PhonemeType.Vowel }
            else
                change.affix.dropWhile { it.getSubstitutePhoneme()?.type == PhonemeType.Vowel }

            listOf(
                change.copy(
                    phonemeMatchers = listOf(TypePositionMatcher(PhonemeType.Consonant, isBeginning)),
                    matchedPhonemesSubstitution = listOf(PassingPhonemeSubstitution()),
                ),
                change.copy(
                    phonemeMatchers = listOf(TypePositionMatcher(PhonemeType.Vowel, isBeginning)),
                    matchedPhonemesSubstitution = listOf(PassingPhonemeSubstitution()),
                    affix = vowelAdjacentAffix
                ),
            )
        }
    }

    private fun randomCollisionElimination(
        templateChanges: List<TemplateSingleChange>,
        restrictions: PhoneticRestrictions
    ): List<WordChange> {
        return templateChanges.map { change ->
            var result: WordChange = change
            val borderPhoneme = getBorderPhoneme(change)
                ?: return@map result
            val borderAffixMatcher = when (change.position) {
                Position.Beginning -> change.phonemeMatchers.firstOrNull()
                Position.End -> change.phonemeMatchers.lastOrNull()
            } ?: PassingMatcher
            val hasCollision = when (change.position) {
                Position.Beginning -> restrictions.initialWordPhonemes
                Position.End -> restrictions.finalWordPhonemes
            }.filter { phoneme -> borderAffixMatcher.test(listOf(Syllable(phoneme))) }
                .any { phoneme -> doPhonemesCollide(phoneme, borderPhoneme) }

            if (hasCollision)
                result = removeCollision(change, restrictions, borderPhoneme) ?: result

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
                generateSyllableAffix(restrictions, null, null)
            val newBorderPhoneme = when (wordChange.position) {
                Position.Beginning -> newChange.last()
                Position.End -> newChange[0]
            }.phoneme
            if (!doPhonemesCollide(newBorderPhoneme, borderPhoneme)) {
                return TemplateSequenceChange(
                    makeTemplateChangeWithBorderPhoneme(wordChange, newChange, borderPhoneme),
                    wordChange
                )
            }
        }
        return null
    }

    private fun makeTemplateChangeWithBorderPhoneme(
        oldChange: TemplateSingleChange,
        newAffix: List<PhonemeSubstitution>,
        neededPhoneme: Phoneme
    ): TemplateSingleChange {
        val isBeginning = oldChange.position == Position.Beginning
        val singleMatcher = listOf(PhonemeMatcher(neededPhoneme, isBeginning))
        val singleSubstitution = listOf(PassingPhonemeSubstitution())
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

        return TemplateSingleChange(oldChange.position, phonemeMatcher, matchedPhonemeSubstitution, newAffix)
    }

    private fun getBorderPhoneme(singleChange: TemplateSingleChange) = when (singleChange.position) {
        Position.Beginning -> singleChange.affix.last()
        Position.End -> singleChange.affix.first()
    }.getSubstitutePhoneme()

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
        val irregularityBias = RandomSingleton.random.nextDouble(0.1, 1.0)
        val biasDecrease = 0.975
        val newChangeParadigms = mutableMapOf<TypedSpeechPart, SpeechPartChangeParadigm>()

        for (speechPart in paradigm.wordChangeParadigm.speechParts) {
            val speechPartParadigm = paradigm.wordChangeParadigm.getSpeechPartParadigm(speechPart)
            val newApplicators = speechPartParadigm.orderedApplicators
                .mapIndexed { i, (c, m) ->
                    val newValueMap = m.map { (v, a) ->
                        val spWords = lexis.getBySpeechPart(speechPart)
                            .groupBy { it.semanticsCore.commonness }
                            .map { (c, ws) -> c to ws.shuffled(RandomSingleton.random) }
                            .sortedByDescending { (c) -> c }
                            .flatMap { it.second }

                        var currentBias = irregularityBias
                        val irregularWords = spWords.takeWhile {
                            (currentBias * it.semanticsCore.commonness).chanceOf<Boolean> {
                                currentBias *= biasDecrease

                                true
                            } ?: false
                        }

                        v to generateIrregularApplicator(irregularWords, a, i)
                    }.toMap()

                    c to newValueMap
                }.toMap()

            newChangeParadigms[speechPart] = speechPartParadigm.copy(applicators = newApplicators)
        }

        return paradigm.copy(
            wordChangeParadigm = paradigm.wordChangeParadigm.copy(speechPartChangeParadigms = newChangeParadigms)
        )
    }

    private fun generateIrregularApplicator(
        words: List<Word>,
        oldApplicator: CategoryApplicator,
        applicatorIndex: Int
    ): CategoryApplicator {
        if (words.isEmpty())
            return oldApplicator
        else return oldApplicator


//        //TODO many new applicators
//        //TODO only few remains
////        val applicatorSequence = listOf(generateIrregularApplicator(app))
//        val applicatorSequence = listOf(applica)
//
//        return FilterApplicator(applicatorSequence + (oldApplicator to listOf()))
    }
}


enum class AffixTypes(override val probability: Double) : SampleSpaceObject {
    UniversalAffix(100.0),
    PhonemeTypeAffix(50.0)
}
