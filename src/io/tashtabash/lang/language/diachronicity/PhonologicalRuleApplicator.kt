package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.NoPhonemeException
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.realization.*
import io.tashtabash.lang.language.derivation.*
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
import io.tashtabash.lang.language.phonology.matcher.unitePhonemeMatchersAfterSubstitution
import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.lang.language.syntax.ChangeParadigm
import kotlin.math.max


class PhonologicalRuleApplicator(private val forcedApplication: Boolean = false) {
    private val derivationCache = mutableMapOf<Derivation, Derivation>()
    private val compoundCache = mutableMapOf<Compound, Compound>()
    private var isChangeApplied = false

    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    private fun cleanState() {
        derivationCache.clear()
        compoundCache.clear()
        isChangeApplied = false
    }

    fun applyPhonologicalRule(language: Language, rule: PhonologicalRule): Language {
        cleanState()

        if (RuleApplicabilityAnalyser(language).isSandhi(rule) && !forcedApplication) {
            _messages += "Rule $rule is added to sandhi rules"

            val newWordChangeParadigm = language.changeParadigm.wordChangeParadigm.copy(
                sandhiRules = language.changeParadigm.wordChangeParadigm.sandhiRules + rule
            )

            return language.copy(
                changeParadigm = language.changeParadigm.copy(wordChangeParadigm = newWordChangeParadigm)
            )
        }

        val shiftedDerivationParadigm = applyPhonologicalRule(language.derivationParadigm, rule)
        var shiftedChangeParadigm = applyPhonologicalRule(language.changeParadigm, rule)
        var shiftedLexis = applyPhonologicalRule(language.lexis, rule)

        if (!isChangeApplied) {
            _messages += "Rule $rule didn't have any effect on the language"
            return language
        }

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

        val validityReport = areChangesValid(shiftedLexis, shiftedChangeParadigm)
        if (validityReport.isFailure) {
            _messages += "Can't apply rule $rule: " +
                    "the resulting language has incorrect forms: '${validityReport.exceptionOrNull()?.message}'"
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
        ).removeUnusedRules()
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
        derivationCache[derivation]?.let {
            return it
        }

        val shiftedAffix = applyPhonologicalRule(derivation.affix, rule)
        val shiftedDerivation = derivation.copy(affix = shiftedAffix)

        derivationCache[derivation] = shiftedDerivation

        return shiftedDerivation
    }

    fun applyPhonologicalRule(compound: Compound, rule: PhonologicalRule): Compound {
        compoundCache[compound]?.let {
            return it
        }

        return try {
            val shiftedInfix = applyPhonologicalRule(
                compound.infix.phonemes.map { it to listOf() },
                rule,
                addStartBoundary = false,
                addEndBoundary = false
            )
            val shiftedCompound = compound.copy(infix = PhonemeSequence(shiftedInfix))

            compoundCache[compound] = shiftedCompound

            shiftedCompound
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
                when (templateChange.position) {
                    Position.Beginning -> {
                        try {
                            val prefixPhonemes = templateChange.affix
                                .map { it.exactPhoneme }
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
                            for (i in 1 until rule.matchers.size)
                                applyRuleAcrossBoundaries(rule, i, baseRawPhonemes, templateChange)
                                    ?.let { changes += it }

                            val resultChange = createSimplifiedTemplateChange(changes.reversed())
                            if (resultChange != templateChange)
                                isChangeApplied = true

                            return resultChange
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

    private fun applyRuleAcrossBoundaries(
        rule: PhonologicalRule,
        i: Int,
        baseRawPhonemes: List<ChangingPhoneme>,
        templateChange: TemplateSingleChange,
    ): TemplateSingleChange? {
        val prefixMatchers = rule.matchers.dropLast(i)
        val stemMatchers = rule.matchers.takeLast(i)
        val phonemeWindow = baseRawPhonemes.takeLast(prefixMatchers.size)

        // Check if the morpheme matches the prefix of the phonologicalRule
        if (phonemeWindow.size != prefixMatchers.size)
            return null
        if (!prefixMatchers.match(phonemeWindow))
            return null

        val substitutionShift = baseRawPhonemes.size -
                prefixMatchers.size +
                rule.precedingMatchers.size

        val rawPhonemes = baseRawPhonemes.toMutableList()
        substitutePhonemes(rawPhonemes, substitutionShift, rule.resultingPhonemes)
        val newAffix = clearChangingPhonemes(rawPhonemes)
            .map { ExactPhonemeSubstitution(it) }

        // Create new stem matchers accounting for the suffix of the phonologicalRule
        val newMatchers = unitePhonemeMatchersAfterSubstitution(
            templateChange.phonemeMatchers,
            templateChange.matchedPhonemesSubstitution,
            stemMatchers
        )
        if (newMatchers.any { it == null })
            return null
        // The morpheme can't be attached to a word border
        if (newMatchers == listOf(BorderPhonemeMatcher))
            return null

        // Create new substitutions accounting for the suffix of the phonologicalRule
        val stemSubstitutionsSize = max(0, stemMatchers.size - rule.followingMatchers.size)
        val stemSubstitutions = rule.resultingPhonemes
            .takeLast(stemSubstitutionsSize)
        val stemSubstitutionsShift = max(
            0,
            stemMatchers.size - rule.followingMatchers.size - rule.targetMatchers.size
        )
        val newSubstitutions = unitePhonemeSubstitutions(
            templateChange.matchedPhonemesSubstitution,
            (1..stemSubstitutionsShift).map { null } + stemSubstitutions
        )
        val passingMatcherSuffix = (newSubstitutions.size until newMatchers.size)
            .map { PassingPhonemeSubstitution }

        return TemplateSingleChange(
            templateChange.position,
            newMatchers.filterIsInstance<PhonemeMatcher>(),
            newSubstitutions + passingMatcherSuffix,
            newAffix
        )
    }

    fun applyPhonologicalRule(changeParadigm: ChangeParadigm, rule: PhonologicalRule): ChangeParadigm =
        changeParadigm.mapApplicators { applyPhonologicalRule(it, rule) }

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
            is WordReduplicationCategoryApplicator, PassingCategoryApplicator -> categoryApplicator.copy()
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
            val morphemes = matchMorphemes(word, rawPhonemes.drop(1).dropLast(1))
            val resultPhonemes = clearChangingPhonemes(rawPhonemes)

            val syllableTemplate =
                if (rule.allowSyllableStructureChange)
                    analyzeSyllableStructure(resultPhonemes).getOrNull()
                else
                    word.syllableTemplate
            val syllables = syllableTemplate
                ?.splitOnSyllables(PhonemeSequence(resultPhonemes))
                ?.mapIndexed { j, syllable -> syllable.copy(prosody = prosodies[j]) }
            if (syllables == null) {
                _messages += "Can't split the word '$word' on syllables after applying the rule, reverting the word"
                return word
            }
            if (syllables.size != prosodies.size) {
                _messages += "The word '$word' unexpectedly changed the number of syllables after applying the rule, " +
                        "reverting the word"
                return word
            }

            val changeHistory = word.semanticsCore.changeHistory?.let { applyPhonologicalRule(it, rule) }

            return word.copy(
                syllables = syllables,
                syllableTemplate = syllableTemplate,
                morphemes = morphemes,
                semanticsCore = word.semanticsCore.copy(changeHistory = changeHistory)
            )
        } catch (e: NoPhonemeException) {
            _messages += "Can't apply the rule for the word '${word}': ${e.message}"
            return word
        }
    }

    fun applyPhonologicalRule(changeHistory: ChangeHistory, rule: PhonologicalRule): ChangeHistory =
        when (changeHistory) {
            is DerivationHistory ->
                changeHistory.copy(derivation = applyPhonologicalRule(changeHistory.derivation, rule))
            is CompoundHistory ->
                changeHistory.copy(compound = applyPhonologicalRule(changeHistory.compound, rule))
            else ->
                throw LanguageException("Unknown ChangeHistory $changeHistory")
        }

    fun applyPhonologicalRule(phonemes: List<ChangingPhoneme>, rule: PhonologicalRule): List<ChangingPhoneme> {
        val result = phonemes.toMutableList()
        var i = 0

        while (i + rule.matchers.size <= result.size) {
            val phonemeWindow = result.drop(i)
            val isMatch = rule.matchers
                .match(phonemeWindow)
            if (isMatch) {
                substitutePhonemes(result, i + rule.precedingMatchers.size, rule.resultingPhonemes)
                isChangeApplied = true
            }
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
        substitutions: List<PhonemeSubstitution>
    ) {
        var curShift = shift
        for (substitution in substitutions) {
            //Allow adding to the end only if a substitution is an epenthesis
            if (curShift >= phonemes.size && substitution !is EpenthesisSubstitution)
                break
            if (curShift < 0) {
                curShift++
                continue
            }

            val oldPhoneme = phonemes.getOrNull(curShift)
            val newPhonemes = substitution.substitute(oldPhoneme?.phoneme)
            if (newPhonemes.isEmpty())
                phonemes[curShift] = ChangingPhoneme.DeletedPhoneme
            else {
                val newExactPhonemes =
                    if (substitution.isOriginalPhonemeChanged)
                        transferOldProsody(oldPhoneme!!, newPhonemes)
                    else
                        newPhonemes.map {
                            // Assume that only vowels are syllabic
                            val defaultProsody = if (it.type == PhonemeType.Vowel) listOf<Prosody>() else null
                            ChangingPhoneme.ExactPhoneme(it, defaultProsody, true)
                        }

                if (curShift != phonemes.size) {
                    phonemes.removeAt(curShift)
                    phonemes.addAll(curShift, newExactPhonemes)
                } else
                    phonemes.addAll(newExactPhonemes)
                curShift += newExactPhonemes.size

                if (!substitution.isOriginalPhonemeChanged && oldPhoneme != null) {
                    phonemes.add(curShift, oldPhoneme)
                }
            }
        }
    }

    private fun transferOldProsody(oldPhoneme: ChangingPhoneme, newPhonemes: List<Phoneme>): List<ChangingPhoneme> {
        val newProsody = oldPhoneme.prosody

        if (newPhonemes.size == 1)
            return listOf(ChangingPhoneme.ExactPhoneme(newPhonemes[0], newProsody))

        val newExactPhonemes = newPhonemes.map { p ->
            ChangingPhoneme.ExactPhoneme(p, newProsody.takeIf { p == oldPhoneme.phoneme })
        }
        if (newExactPhonemes.none { it.prosody == newProsody })
            _messages += "Prosody $newProsody lost: can't distribute across multiple phonemes: $newProsody"
        return newExactPhonemes
    }

    private fun matchMorphemes(oldWord: Word, rawNewPhonemes: List<ChangingPhoneme>): List<MorphemeData> =
        matchMorphemes(oldWord.morphemes, rawNewPhonemes)

    private fun matchMorphemes(oldMorphemes: List<MorphemeData>, rawNewPhonemes: List<ChangingPhoneme>): List<MorphemeData> {
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

        for (changingPhoneme in rawNewPhonemes) {
            // Add zero morphemes immediately and don't start the next morpheme if
            //  an epenthesis is encountered (add epenthesis to the current non-zero morpheme)
            if (newMorphemeSize == 0 || !changingPhoneme.isEpenthesis)
                addMorphemeIfReady()

            if (changingPhoneme.phoneme != null)
                newMorphemeSize++

            if (!changingPhoneme.isEpenthesis)
                oldMorphemePhonemeIdx++
        }

        addMorphemeIfReady()

        return newMorphemes
    }
}
