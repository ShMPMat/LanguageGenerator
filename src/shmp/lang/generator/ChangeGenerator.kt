package shmp.lang.generator

import shmp.lang.generator.util.SyllablePosition
import shmp.lang.generator.util.SyllableRestrictions
import shmp.lang.language.category.paradigm.SpeechPartChangeParadigm
import shmp.lang.language.category.realization.CategoryApplicator
import shmp.lang.language.lexis.Lexis
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.morphem.change.*
import shmp.lang.language.morphem.change.matcher.PassingMatcher
import shmp.lang.language.morphem.change.matcher.PhonemeMatcher
import shmp.lang.language.morphem.change.matcher.TypePositionMatcher
import shmp.lang.language.morphem.change.substitution.PassingPositionSubstitution
import shmp.lang.language.morphem.change.substitution.PhonemePositionSubstitution
import shmp.lang.language.morphem.change.substitution.PositionSubstitution
import shmp.lang.language.phonology.*
import shmp.lang.language.syntax.ChangeParadigm
import shmp.random.SampleSpaceObject
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement


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

                randomDoubleEdgeLettersElimination(templates, restrictions)
            }
            AffixTypes.PhonemeTypeAffix -> {
                val templates = PhonemeType.values().map {
                    val affix = generateSyllableAffix(
                        restrictions,
                        hasInitial == true || it == PhonemeType.Vowel,
                        it == PhonemeType.Vowel && position == Position.End
                    )
                    val substitutions = listOf(PassingPositionSubstitution())
                    val isBeginning = position == Position.Beginning

                    TemplateSingleChange(position, listOf(TypePositionMatcher(it, isBeginning)), substitutions, affix)
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
                .any { phoneme -> doesPhonemesCollide(phoneme, borderPhoneme) }

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
            if (!doesPhonemesCollide(newBorderPhoneme, borderPhoneme)) {
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
        newAffix: List<PositionSubstitution>,
        neededPhoneme: Phoneme
    ): TemplateSingleChange {
        val isBeginning = oldChange.position == Position.Beginning
        val singleMatcher = listOf(PhonemeMatcher(neededPhoneme, isBeginning))
        val singleSubstitution = listOf(PassingPositionSubstitution())
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
        .map { PhonemePositionSubstitution(it) }


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
