package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.containers.NoPhonemeException
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.analyzer.analyzePhonemes
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
import io.tashtabash.lang.language.phonology.matcher.match
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

        if (RuleApplicabilityAnalyser(language).isSandhi(rule) && !forcedApplication)
            return addSandhiRule(language, rule)

        val shiftedDerivationParadigm = applyPhonologicalRule(language.derivationParadigm, rule)
        var shiftedChangeParadigm = applyPhonologicalRule(language.changeParadigm, rule)
        var shiftedLexis = applyPhonologicalRule(language.lexis, rule)

        if (!isChangeApplied) {
            _messages += "Rule $rule didn't have any effect on the language"
            return language
        }

        if (rule.allowSyllableStructureChange) {
            val result = changeSyllableStructure(shiftedLexis, shiftedChangeParadigm)
            shiftedLexis = result.first
            shiftedChangeParadigm = result.second
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

    private fun addSandhiRule(language: Language, rule: PhonologicalRule): Language {
        if (!isSandhiRuleApplied(language, rule)) {
            _messages += "Rule $rule didn't have any effect on the language"
            return language
        }

        _messages += "Rule $rule is added to sandhi rules"

        val newWordChangeParadigm = language.changeParadigm.wordChangeParadigm.copy(
            sandhiRules = language.changeParadigm.wordChangeParadigm.sandhiRules + rule
        )
        val newPhonemes = (language.phonemeContainer.phonemes + analyzePhonemes(rule, language.phonemeContainer))
            .distinct()

        return language.copy(
            changeParadigm = language.changeParadigm.copy(wordChangeParadigm = newWordChangeParadigm),
            phonemeContainer = ImmutablePhonemeContainer(newPhonemes)
        )
    }

    private fun isSandhiRuleApplied(language: Language, rule: PhonologicalRule): Boolean {
        val forcedApplicator = PhonologicalRuleApplicator(true)
        forcedApplicator.applyPhonologicalRule(language, rule)

        return forcedApplicator.messages.lastOrNull() != "Rule $rule didn't have any effect on the language"
    }

    private fun changeSyllableStructure(lexis: Lexis, changeParadigm: ChangeParadigm): Pair<Lexis, ChangeParadigm> {
        val result = fixSyllableStructure(lexis, changeParadigm)

        result.exceptionOrNull()
            ?.message
            ?.let { _messages += it }

        return (result.getOrNull()?.first ?: lexis) to
                (result.getOrNull()?.second ?: changeParadigm)
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
                try {
                    val newRules = (templateChange.rule + rule).map {
                        TemplateSingleChange(
                            templateChange.position,
                            it.copy(allowSyllableStructureChange = false)
                        )
                    }

                    val resultChange = createSimplifiedTemplateChange(newRules)
                    if (resultChange != templateChange)
                        isChangeApplied = true

                    return resultChange
                } catch (e: NoPhonemeException) {
                    _messages += "Can't apply the rule for the change '${templateChange}': ${e.message}"
                    return templateChange
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

    fun applyPhonologicalRule(changeParadigm: ChangeParadigm, rule: PhonologicalRule): ChangeParadigm =
        changeParadigm.mapApplicators { applyPhonologicalRule(it, rule) }
            .let {
                val shiftedSandhiRules = it.wordChangeParadigm.sandhiRules
                    .flatMap { s -> s * rule }
                    .distinct()
                it.copy(wordChangeParadigm = it.wordChangeParadigm.copy(sandhiRules = shiftedSandhiRules))
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

    fun applyPhonologicalRule(word: Word, rule: PhonologicalRule, doChangeHistory: Boolean = true): Word {
        try {
            val rawPhonemes = applyPhonologicalRule(getChangingPhonemes(word), rule)
            val prosodies = rawPhonemes.filterIsInstance<ChangingPhoneme.ExactPhoneme>()
                .mapNotNull { it.prosody }
            val morphemes = matchMorphemes(word, rawPhonemes.subList(1, rawPhonemes.size - 1))
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

            val changeHistory =
                if (doChangeHistory)
                    word.semanticsCore.changeHistory?.let { applyPhonologicalRule(it, rule) }
                else
                    word.semanticsCore.changeHistory

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
            val phonemeWindow = result.subList(i, result.size)
            val isMatch = rule.matchers
                .match(phonemeWindow)
            if (isMatch) {
                substitutePhonemes(result, i + rule.precedingMatchers.size, rule.substitutions)
                isChangeApplied = true
                i += max(rule.precedingMatchers.size + rule.substitutions.size, 1)
                // Stop if the end has been matched
                if (rule.matchers.last() == BorderPhonemeMatcher)
                    break
            } else
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

    private fun substitutePhonemes(
        phonemes: MutableList<ChangingPhoneme>,
        shift: Int,
        substitutions: List<PhonemeSubstitution>
    ) {
        var curShift = max(shift, 0) // Skip to changes, there's nothing to apply with curShift < 0
        for (substitution in substitutions) {
            //Allow adding to the end only if a substitution is an epenthesis
            if (curShift >= phonemes.size && substitution !is EpenthesisSubstitution)
                break

            val oldPhoneme = phonemes.getOrNull(curShift)
            val newPhonemes = substitution.substitute(oldPhoneme?.phoneme)
            if (newPhonemes.isEmpty()) {
                if (oldPhoneme != ChangingPhoneme.Boundary)
                    phonemes[curShift] = ChangingPhoneme.DeletedPhoneme
                curShift++
            } else {
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
