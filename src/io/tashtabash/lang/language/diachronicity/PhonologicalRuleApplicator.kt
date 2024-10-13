package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.NoPhonemeException
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.realization.*
import io.tashtabash.lang.language.derivation.Compound
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationParadigm
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.morphem.Prefix
import io.tashtabash.lang.language.morphem.Suffix
import io.tashtabash.lang.language.morphem.change.*
import io.tashtabash.lang.language.morphem.change.substitution.*
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.phonology.matcher.BorderPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.unitePhonemeMatchers
import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.lang.language.syntax.ChangeParadigm
import kotlin.math.max


class PhonologicalRuleApplicator {
    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    fun applyPhonologicalRule(language: Language, rule: PhonologicalRule): Language {
        val shiftedDerivationParadigm = applyPhonologicalRule(language.derivationParadigm, rule)
        var shiftedChangeParadigm = applyPhonologicalRule(language.changeParadigm, rule)
        var shiftedLexis = applyPhonologicalRule(language.lexis, rule)

        if (rule.allowSyllableStructureChange) {
            val result = fixSyllableStructure(shiftedLexis, shiftedChangeParadigm)

            result.exceptionOrNull()
                ?.message
                ?.let { _messages += it }

            shiftedLexis = result.getOrNull()
                ?.first
                ?: shiftedLexis
            shiftedChangeParadigm = result.getOrNull()
                ?.second
                ?: shiftedChangeParadigm
        }

        val changeValidityReport = areChangesValid(shiftedLexis, shiftedChangeParadigm)
        if (changeValidityReport.isFailure) {
            _messages += "Cannot apply rule $rule: " +
                    "the resulting language has incorrect forms: '${changeValidityReport.exceptionOrNull()?.message}'"
            return language
        }

        val newPhonemes = analyzePhonemes(shiftedLexis, shiftedDerivationParadigm, shiftedChangeParadigm)

        return Language(
            shiftedLexis,
            newPhonemes,
            language.stressType,
            language.restrictionsParadigm,
            shiftedDerivationParadigm,
            shiftedChangeParadigm
        )
    }

    fun applyPhonologicalRule(derivationParadigm: DerivationParadigm, rule: PhonologicalRule): DerivationParadigm {
        val shiftedDerivations = derivationParadigm.derivations.map {
            applyPhonologicalRule(it, rule)
        }
        val shiftedCompounds = derivationParadigm.compounds.map {
            applyPhonologicalRule(it, rule)
        }
        return DerivationParadigm(shiftedDerivations, shiftedCompounds)
    }

    fun applyPhonologicalRule(derivation: Derivation, rule: PhonologicalRule): Derivation {
        val shiftedAffix = applyPhonologicalRule(derivation.affix, rule)

        return derivation.copy(affix = shiftedAffix)
    }

    fun applyPhonologicalRule(compound: Compound, rule: PhonologicalRule): Compound {
        return try {
            val shiftedInfix = applyPhonologicalRule(
                compound.infix.phonemes.map { it to listOf() },
                rule,
                addStartBoundary = false,
                addEndBoundary = false
            )

            compound.copy(infix = PhonemeSequence(shiftedInfix))
        } catch (e: NoPhonemeException) {
            _messages += "Can't apply the rule for the infix '${compound.infix.phonemes}': ${e.message}"
            compound
        }
    }

    fun applyPhonologicalRule(affix: Affix, rule: PhonologicalRule): Affix = when (affix) {
        is Prefix -> {
            Prefix(applyPhonologicalRule(affix.templateChange, rule))
        }
        is Suffix -> {
            Suffix(applyPhonologicalRule(affix.templateChange, rule))
        }
        else -> throw LanguageException("Unknown affix '$affix'")
    }

    fun applyPhonologicalRule(templateChange: TemplateChange, rule: PhonologicalRule): TemplateChange {
        when (templateChange) {
            is TemplateSingleChange -> {
                templateChange.affix
                when (templateChange.position) {
                    Position.Beginning -> {
                        try {
                            val prefixPhonemes = templateChange.affix
                                .map { it.getSubstitutePhoneme() }
                            var baseRawPhonemes: List<ChangingPhoneme> = getChangingPhonemes(
                                prefixPhonemes.map { it to listOf() },
                                addStartBoundary = true,
                                addEndBoundary = false
                            )
                            baseRawPhonemes = applyPhonologicalRule(baseRawPhonemes, rule)
                            val newBaseAffix = clearChangingPhonemes(baseRawPhonemes)
                                .map { ExactPhonemeSubstitution(it) }

                            val changes = mutableListOf(
                                templateChange.copy(affix = newBaseAffix)
                            )

                            // Calculate new TemplateChanges for all applications of the rule across morpheme boundaries
                            for (i in 1 until rule.matchers.size) {
                                val prefixMatchers = rule.matchers.dropLast(i)
                                val stemMatchers = rule.matchers.takeLast(i)
                                val phonemeWindow = baseRawPhonemes.takeLast(prefixMatchers.size)

                                // Check if the morpheme matches the prefix of the phonologicalRule
                                if (phonemeWindow.size != prefixMatchers.size)
                                    continue
                                if (!prefixMatchers.match(phonemeWindow))
                                    continue

                                val substitutionShift = baseRawPhonemes.size -
                                        prefixMatchers.size +
                                        rule.precedingMatchers.size

                                val rawPhonemes = baseRawPhonemes.toMutableList()
                                substitutePhonemes(rawPhonemes, substitutionShift, rule.resultingPhonemes)
                                val newAffix = clearChangingPhonemes(rawPhonemes)
                                    .map { ExactPhonemeSubstitution(it) }

                                // Create new stem matchers accounting for the suffix of the phonologicalRule
                                val newMatchers = unitePhonemeMatchers(stemMatchers, templateChange.phonemeMatchers)
                                if (newMatchers.any { it == null })
                                    continue
                                // The morpheme can't be attached to a word border
                                if (newMatchers == listOf(BorderPhonemeMatcher))
                                    continue

                                // Create new substitutions accounting for the suffix of the phonologicalRule
                                val stemSubstitutionsSize = max(
                                    stemMatchers.size - rule.followingMatchers.size,
                                    0
                                )
                                val stemSubstitutions = rule.resultingPhonemes
                                    .takeLast(stemSubstitutionsSize)
                                val stemSubstitutionsShift = max(
                                    stemMatchers.size - rule.followingMatchers.size - rule.targetMatchers.size,
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
                        } catch (e: NoPhonemeException) {
                            _messages += "Can't apply the rule for the change '${templateChange}': ${e.message}"
                            return templateChange
                        }
                    }
                    Position.End -> {
                        return applyPhonologicalRule(templateChange.mirror(), rule.mirror())
                            .mirror()
                    }
                }
            }
            is TemplateSequenceChange -> {
                val shiftedTemplateChanges = templateChange.changes
                    .map { applyPhonologicalRule(it, rule) }

                return createSimplifiedTemplateChange(shiftedTemplateChanges)
            }
            else -> throw LanguageException("Unknown template change '$templateChange'")
        }
    }

    fun applyPhonologicalRule(changeParadigm: ChangeParadigm, rule: PhonologicalRule): ChangeParadigm {
        val shiftedWordChangeParadigm = changeParadigm.wordChangeParadigm
            .mapApplicators { applyPhonologicalRule(it, rule) }

        return changeParadigm.copy(wordChangeParadigm = shiftedWordChangeParadigm)
    }

    fun applyPhonologicalRule(categoryApplicator: CategoryApplicator, rule: PhonologicalRule): CategoryApplicator =
        when (categoryApplicator) {
            is AffixCategoryApplicator -> AffixCategoryApplicator(
                applyPhonologicalRule(categoryApplicator.affix, rule),
                categoryApplicator.type
            )
            is ConsecutiveApplicator -> ConsecutiveApplicator(
                categoryApplicator.applicators.map { applyPhonologicalRule(it, rule) }
            )
            is FilterApplicator -> FilterApplicator(
                categoryApplicator.applicators.map { (applicator, words) ->
                    applyPhonologicalRule(applicator, rule) to
                            words.map { applyPhonologicalRule(it, rule) }
                }
            )
            is ReduplicationCategoryApplicator, PassingCategoryApplicator -> categoryApplicator.copy()
            is WordCategoryApplicator -> categoryApplicator.copy(
                applyPhonologicalRule(categoryApplicator.word, rule),
            )
            else -> throw LanguageException("Unknown category applicator '$categoryApplicator'")
        }

    fun applyPhonologicalRule(lexis: Lexis, rule: PhonologicalRule): Lexis {
        val shiftedWords = lexis.words.map {
            applyPhonologicalRule(it, rule)
        }

        return lexis.shift(shiftedWords)
    }

    fun applyPhonologicalRule(word: Word, rule: PhonologicalRule): Word {
        try {
            val rawPhonemes = applyPhonologicalRule(getChangingPhonemes(word), rule)
            val prosodies = rawPhonemes.filterIsInstance<ChangingPhoneme.ExactPhoneme>()
                .mapNotNull { it.prosody }
            val morphemes = matchMorphemes(word, rawPhonemes.drop(1).dropLast(1).map { it.phoneme })
            val resultPhonemes = clearChangingPhonemes(rawPhonemes)

            val syllableTemplate =
                if (rule.allowSyllableStructureChange)
                    analyzeSyllableStructure(resultPhonemes).getOrNull()
                else
                    word.syllableTemplate
            val syllables = syllableTemplate
                ?.splitOnSyllables(PhonemeSequence(resultPhonemes))
                ?.mapIndexed { j, syllable -> syllable.copy(prosodicEnums = prosodies[j]) }
            if (syllables == null) {
                _messages += "Can't split the word '$word' on syllables after applying the rule, reverting the word"
                return word
            }
            if (syllables.size != prosodies.size) {
                _messages += "The word '$word' unexpectedly changed the number of syllables after applying the rule, " +
                        "reverting the word"
                return word
            }

            return word.copy(syllables = syllables, syllableTemplate = syllableTemplate, morphemes = morphemes)
        } catch (e: NoPhonemeException) {
            _messages += "Can't apply the rule for the word '${word}': ${e.message}"
            return word
        }
    }

    fun applyPhonologicalRule(phonemes: List<ChangingPhoneme>, rule: PhonologicalRule): List<ChangingPhoneme> {
        val result = phonemes.toMutableList()
        var i = 0

        while (i + rule.matchers.size <= phonemes.size) {
            val phonemeWindow = phonemes.drop(i)
            val isMatch = rule.matchers
                .match(phonemeWindow)
            if (isMatch)
                substitutePhonemes(
                    result,
                    i + rule.precedingMatchers.size,
                    rule.resultingPhonemes
                )
            i++
        }

        return result
    }

    fun applyPhonologicalRule(
        phonemes: List<Pair<Phoneme, List<Prosody>?>>,
        rule: PhonologicalRule,
        addStartBoundary: Boolean,
        addEndBoundary: Boolean
    ): List<Phoneme> {
        val rawPhonemes: List<ChangingPhoneme> = getChangingPhonemes(phonemes, addStartBoundary, addEndBoundary)
        val changedPhonemes = applyPhonologicalRule(rawPhonemes, rule)
        return clearChangingPhonemes(changedPhonemes)
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
            val newProsody = phonemes[shift + j].let {
                if (it is ChangingPhoneme.ExactPhoneme)
                    it.prosody
                else null
            }
            phonemes[shift + j] = newPhoneme
                ?.let { ChangingPhoneme.ExactPhoneme(it, newProsody) }
                ?: ChangingPhoneme.DeletedPhoneme
        }
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
}
