package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.category.realization.*
import io.tashtabash.lang.language.derivation.Compound
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationParadigm
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.morphem.Prefix
import io.tashtabash.lang.language.morphem.Suffix
import io.tashtabash.lang.language.morphem.change.*
import io.tashtabash.lang.language.morphem.change.substitution.ExactPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PassingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.unitePhonemeSubstitutions
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.phonology.matcher.BorderPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.unitePhonemeMatchers
import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.random.singleton.randomElementOrNull
import kotlin.math.max


class PhonologicalRuleApplicator {
    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    fun applyRandomPhonologicalRule(
        language: Language,
        phonologicalRulesContainer: PhonologicalRulesContainer
    ): Language {
        val phonologicalRule = phonologicalRulesContainer
            .getApplicableRules(language)
            .randomElementOrNull()
        if (phonologicalRule == null) {
            _messages += "No changes available for the language"
            return language
        }
        _messages += "Applying rule '$phonologicalRule'"

        return applyPhonologicalRule(language, phonologicalRule)
    }

    fun applyPhonologicalRule(language: Language, phonologicalRule: PhonologicalRule): Language {
        val shiftedDerivations = language.derivationParadigm.derivations.map {
            applyPhonologicalRule(it, phonologicalRule)
        }
        val shiftedCompounds = language.derivationParadigm.compounds.map {
            applyPhonologicalRule(it, phonologicalRule)
        }
        val shiftedDerivationParadigm = DerivationParadigm(shiftedDerivations, shiftedCompounds)

        val shiftedSpeechPartChangeParadigms =
            language.changeParadigm.wordChangeParadigm.speechPartChangeParadigms.mapValues {
                applyPhonologicalRule(it.value, phonologicalRule)
            }
        val shiftedWordChangeParadigm = WordChangeParadigm(
            language.changeParadigm.wordChangeParadigm.categories,
            shiftedSpeechPartChangeParadigms
        )
        val shiftedChangeParadigm = language.changeParadigm.copy(wordChangeParadigm = shiftedWordChangeParadigm)

        val shiftedWords = language.lexis.words.map {
            applyPhonologicalRule(it, phonologicalRule)
        }
        val shiftedLexis = language.lexis.copy(words = shiftedWords)

        val newPhonemes = analyzePhonemes(shiftedLexis, shiftedDerivationParadigm, shiftedChangeParadigm)

        return Language(
            shiftedLexis,
            newPhonemes,
            language.stressType,
            language.restrictionsParadigm,
            shiftedDerivationParadigm,
            shiftedChangeParadigm,
        )
    }

    fun applyPhonologicalRule(derivation: Derivation, phonologicalRule: PhonologicalRule): Derivation {
        val shiftedAffix = applyPhonologicalRule(derivation.affix, phonologicalRule)

        return derivation.copy(affix = shiftedAffix)
    }

    fun applyPhonologicalRule(compound: Compound, phonologicalRule: PhonologicalRule): Compound {
        val changingPhonemes = getChangingPhonemes(
            compound.infix.phonemes,
            addStartBoundary = false,
            addEndBoundary = false
        )
        val shiftedInfix = applyPhonologicalRule(changingPhonemes, phonologicalRule)

        return compound.copy(infix = PhonemeSequence(clearChangingPhonemes(shiftedInfix)))
    }

    fun applyPhonologicalRule(affix: Affix, phonologicalRule: PhonologicalRule): Affix = when (affix) {
        is Prefix -> {
            Prefix(applyPhonologicalRule(affix.templateChange, phonologicalRule))
        }
        is Suffix -> {
            Suffix(applyPhonologicalRule(affix.templateChange, phonologicalRule))
        }
        else -> throw LanguageException("Unknown affix '$affix'")
    }

    fun applyPhonologicalRule(templateChange: TemplateChange, phonologicalRule: PhonologicalRule): TemplateChange {
        when (templateChange) {
            is TemplateSingleChange -> {
                templateChange.affix
                when (templateChange.position) {
                    Position.Beginning -> {
                        val prefixPhonemes = templateChange.affix
                            .map { it.getSubstitutePhoneme() }
                        var baseRawPhonemes: List<ChangingPhoneme> = getChangingPhonemes(
                            prefixPhonemes,
                            addStartBoundary = true,
                            addEndBoundary = false
                        )
                        baseRawPhonemes = applyPhonologicalRule(baseRawPhonemes, phonologicalRule)
                        val newBaseAffix = clearChangingPhonemes(baseRawPhonemes)
                            .map { ExactPhonemeSubstitution(it) }

                        val changes = mutableListOf(
                            templateChange.copy(affix = newBaseAffix)
                        )

                        // Calculate new TemplateChanges for all applications of the rule across morpheme boundaries
                        for (i in 1 until phonologicalRule.matchers.size) {
                            val prefixMatchers = phonologicalRule.matchers.dropLast(i)
                            val stemMatchers = phonologicalRule.matchers.takeLast(i)
                            val phonemeWindow = baseRawPhonemes.takeLast(prefixMatchers.size)

                            // Check if the morpheme matches the prefix of the phonologicalRule
                            if (phonemeWindow.size != prefixMatchers.size)
                                continue
                            if (!prefixMatchers.match(phonemeWindow))
                                continue

                            val substitutionShift = baseRawPhonemes.size -
                                    prefixMatchers.size +
                                    phonologicalRule.precedingMatchers.size

                            val rawPhonemes = baseRawPhonemes.toMutableList()
                            substitutePhonemes(rawPhonemes, substitutionShift, phonologicalRule.resultingPhonemes)
                            val newAffix = clearChangingPhonemes(rawPhonemes)
                                .map { ExactPhonemeSubstitution(it) }

                            // Create new stem matchers accounting for the suffix of the phonologicalRule
                            val newMatchersLength = max(stemMatchers.size, templateChange.phonemeMatchers.size)
                            val newMatchers = (0 until newMatchersLength).map { j ->
                                unitePhonemeMatchers(
                                    stemMatchers.getOrNull(j),
                                    templateChange.phonemeMatchers.getOrNull(j)
                                )
                            }
                            if (newMatchers.any { it == null })
                                continue
                            // The morpheme can't be attached to a word border
                            if (newMatchers == listOf(BorderPhonemeMatcher))
                                continue

                            // Create new substitutions accounting for the suffix of the phonologicalRule
                            val stemSubstitutionsSize = max(
                                stemMatchers.size - phonologicalRule.followingMatchers.size,
                                0
                            )
                            val stemSubstitutions = phonologicalRule.resultingPhonemes
                                .takeLast(stemSubstitutionsSize)
                            val stemSubstitutionsShift = max(
                                stemMatchers.size - phonologicalRule.followingMatchers.size - phonologicalRule.targetMatchers.size,
                                0
                            )
                            val newSubstitutions = unitePhonemeSubstitutions(
                                templateChange.matchedPhonemesSubstitution,
                                (1..stemSubstitutionsShift).map { null } + stemSubstitutions
                            )
                            val passingMatcherSuffix = (newSubstitutions.size until newMatchers.size)
                                .map { PassingPhonemeSubstitution }

                            changes += TemplateSingleChange(
                                templateChange.position,
                                newMatchers.filterIsInstance<PhonemeMatcher>(),
                                newSubstitutions + passingMatcherSuffix,
                                newAffix
                            )
                        }

                        return createSimplifiedTemplateChange(changes.reversed())
                    }
                    Position.End -> {
                        return applyPhonologicalRule(templateChange.mirror(), phonologicalRule.mirror())
                            .mirror()
                    }
                }
            }
            is TemplateSequenceChange -> {
                val shiftedTemplateChanges = templateChange
                    .changes
                    .flatMap {
                        val change = applyPhonologicalRule(it, phonologicalRule)

                        if (change is TemplateSequenceChange)
                            change.changes
                        else
                            listOf(change)
                    }

                return createSimplifiedTemplateChange(shiftedTemplateChanges)
            }
            else -> throw LanguageException("Unknown template change '$templateChange'")
        }
    }

    fun applyPhonologicalRule(
        speechPartChangeParadigm: SpeechPartChangeParadigm,
        phonologicalRule: PhonologicalRule
    ): SpeechPartChangeParadigm {
        val applicators = speechPartChangeParadigm.applicators
            .mapValues { (_, values) ->
                values.mapValues { (_, applicator) ->
                    applyPhonologicalRule(applicator, phonologicalRule)
                }
            }

        return speechPartChangeParadigm.copy(applicators = applicators)
    }

    fun applyPhonologicalRule(
        categoryApplicator: CategoryApplicator,
        phonologicalRule: PhonologicalRule
    ): CategoryApplicator = when (categoryApplicator) {
        is AffixCategoryApplicator -> AffixCategoryApplicator(
            applyPhonologicalRule(categoryApplicator.affix, phonologicalRule),
            categoryApplicator.type
        )
        is ConsecutiveApplicator -> ConsecutiveApplicator(
            categoryApplicator.applicators.map { applyPhonologicalRule(it, phonologicalRule) }
        )
        is FilterApplicator -> FilterApplicator(
            categoryApplicator.applicators.map { (applicator, words) ->
                applyPhonologicalRule(applicator, phonologicalRule) to
                        words.map { applyPhonologicalRule(it, phonologicalRule) }
            }
        )
        is PassingCategoryApplicator -> PassingCategoryApplicator
        is PrefixWordCategoryApplicator -> PrefixWordCategoryApplicator(
            applyPhonologicalRule(categoryApplicator.word, phonologicalRule),
            categoryApplicator.latch
        )
        is ReduplicationCategoryApplicator -> ReduplicationCategoryApplicator()
        is SuffixWordCategoryApplicator -> SuffixWordCategoryApplicator(
            applyPhonologicalRule(categoryApplicator.word, phonologicalRule),
            categoryApplicator.latch
        )
        is SuppletionCategoryApplicator -> SuppletionCategoryApplicator(
            applyPhonologicalRule(categoryApplicator.word, phonologicalRule)
        )
        else -> throw LanguageException("Unknown category applicator '$categoryApplicator'")
    }

    fun applyPhonologicalRule(word: Word, phonologicalRule: PhonologicalRule): Word {
        val rawPhonemes = applyPhonologicalRule(getChangingPhonemes(word), phonologicalRule)
        val prosodies = matchProsodies(word, rawPhonemes.drop(1).dropLast(1).map { it.phoneme })
        val morphemes = matchMorphemes(word, rawPhonemes.drop(1).dropLast(1).map { it.phoneme })
        val syllables = word.syllableTemplate
            .splitOnSyllables(PhonemeSequence(clearChangingPhonemes(rawPhonemes)))
            ?.mapIndexed { j, syllable -> syllable.copy(prosodicEnums = prosodies[j]) }
        if (syllables == null) {
            _messages += "Can't split the word '$word' on syllables after applying changes, reverting the word"
            return word
        }
        if (syllables.size != prosodies.size) {
            _messages += "The word '$word' changed the number of syllables after applying changes, reverting the word"
            return word
        }

        return word.copy(syllables = syllables, morphemes = morphemes)
    }

    fun applyPhonologicalRule(
        phonemes: List<ChangingPhoneme>,
        phonologicalRule: PhonologicalRule
    ): List<ChangingPhoneme> {
        val result = phonemes.toMutableList()
        var i = 0

        while (i + phonologicalRule.matchers.size <= phonemes.size) {
            val phonemeWindow = phonemes.drop(i)
            val isMatch = phonologicalRule.matchers
                .match(phonemeWindow)
            if (isMatch)
                substitutePhonemes(
                    result,
                    i + phonologicalRule.precedingMatchers.size,
                    phonologicalRule.resultingPhonemes
                )
            i++
        }

        return result
    }

    private fun List<PhonemeMatcher>.match(phonemeWindow: List<ChangingPhoneme>): Boolean =
        zip(phonemeWindow)
            .all { (matcher, phoneme) -> matcher.match(phoneme) }

    private fun substitutePhonemes(
        phonemes: MutableList<ChangingPhoneme>,
        shift: Int,
        resultingPhonemes: List<PhonemeSubstitution>
    ) {
        for (j in resultingPhonemes.indices) {
            if (shift + j >= phonemes.size)
                break
            if (shift + j < 0)
                continue

            val newPhoneme = resultingPhonemes[j].substitute(phonemes[shift + j].phoneme)
            phonemes[shift + j] =
                newPhoneme?.let { ChangingPhoneme.ExactPhoneme(it) } ?: ChangingPhoneme.DeletedPhoneme
        }
    }

    private fun clearChangingPhonemes(phonemes: List<ChangingPhoneme>): List<Phoneme> =
        phonemes.filterIsInstance<ChangingPhoneme.ExactPhoneme>()
            .map { it.phoneme }

    private fun getChangingPhonemes(word: Word): MutableList<ChangingPhoneme> =
        getChangingPhonemes(word.toPhonemes(), addStartBoundary = true, addEndBoundary = true)

    private fun getChangingPhonemes(
        phonemes: List<Phoneme>,
        addStartBoundary: Boolean,
        addEndBoundary: Boolean
    ): MutableList<ChangingPhoneme> {
        val rawPhonemes = mutableListOf<ChangingPhoneme>()

        if (addStartBoundary)
            rawPhonemes += listOf(ChangingPhoneme.Boundary)
        rawPhonemes += phonemes.map { ChangingPhoneme.ExactPhoneme(it) }
        if (addEndBoundary)
            rawPhonemes += listOf(ChangingPhoneme.Boundary)

        return rawPhonemes
    }

    private fun matchProsodies(oldWord: Word, rawNewPhonemes: List<Phoneme?>): List<List<Prosody>> {
        val prosodyContour = getProsodyContour(oldWord)
        val newProsodies = mutableListOf<List<Prosody>>()
        var oldSyllableIdx = 0

        for ((phoneme, prosodies) in rawNewPhonemes.zip(prosodyContour)) {
            if (prosodies != null) {
                if (phoneme != null)
                    newProsodies += oldWord.syllables[oldSyllableIdx].prosodicEnums

                oldSyllableIdx++
            }
        }

        return newProsodies
    }

    private fun matchMorphemes(oldWord: Word, rawNewPhonemes: List<Phoneme?>): List<MorphemeData> =
        matchMorphemes(oldWord.morphemes, rawNewPhonemes)

    private fun matchMorphemes(oldMorphemes: List<MorphemeData>, rawNewPhonemes: List<Phoneme?>): List<MorphemeData> {
        val newMorphemes = mutableListOf<MorphemeData>()
        var oldMorphemeIdx = 0
        var oldMorphemePhonemeIdx = 0
        var newMorphemeSize = 0

        fun addMorphemeIfReady() {
            while (oldMorphemeIdx < oldMorphemes.size && oldMorphemePhonemeIdx == oldMorphemes[oldMorphemeIdx].size) {
                newMorphemes += oldMorphemes[oldMorphemeIdx].copy(size = newMorphemeSize)
                oldMorphemeIdx++
                oldMorphemePhonemeIdx = 0
                newMorphemeSize = 0
            }
        }

        for (phoneme in rawNewPhonemes) {
            addMorphemeIfReady()

            if (phoneme != null)
                newMorphemeSize++

            oldMorphemePhonemeIdx++

            addMorphemeIfReady()
        }

        return newMorphemes
    }

    // null - not a nucleus; empty list - nucleus w/o prosody
    private fun getProsodyContour(word: Word): List<List<Prosody>?> {
        return word.syllables.flatMap { syllable ->
            val prefix = (0 until syllable.nucleusIdx).map<Int, List<Prosody>?> { null }
            val postfix = (syllable.nucleusIdx + 1 until syllable.size).map<Int, List<Prosody>?> { null }

            prefix + listOf(syllable.prosodicEnums) + postfix
        }
    }
}
