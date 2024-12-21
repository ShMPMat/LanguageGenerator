package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.phonology.matcher.*
import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.lang.language.phonology.prosody.StressType


class RuleApplicabilityAnalyser(val language: Language) {
    fun getApplicableRules(phonologicalRulesContainer: PhonologicalRulesContainer): List<PhonologicalRule> =
        phonologicalRulesContainer.phonologicalRules
            .filter(this::isRuleApplicable)

    fun isRuleApplicable(rule: PhonologicalRule): Boolean =
        rule.matchers
            .all(this::isMatcherApplicable)

    fun isMatcherApplicable(matcher: PhonemeMatcher): Boolean = when (matcher) {
        is ExactPhonemeMatcher ->
            language.phonemeContainer.getPhonemeOrNull(matcher.phoneme.symbol) != null
        is TypePhonemeMatcher ->
            language.phonemeContainer.getPhonemes(matcher.phonemeType).isNotEmpty()
        is AbsentModifierPhonemeMatcher ->
            language.phonemeContainer.getPhonemesNot(matcher.modifiers).isNotEmpty()
        is ModifierPhonemeMatcher ->
            language.phonemeContainer.getPhonemes(matcher.modifiers).isNotEmpty()
        is MulMatcher ->
            matcher.matchers.all { isMatcherApplicable(it) }
        is BorderPhonemeMatcher, PassingPhonemeMatcher ->
            true
        is ProsodyMatcher -> matcher.prosody.all {
            it == Prosody.Stress && language.stressType != StressType.None
        }
        is AbsentProsodyMatcher -> matcher.prosody.all {
            it == Prosody.Stress && language.stressType != StressType.None
        }
        else -> throw LanguageException("Unknown PhonemeMatcher '$matcher'")
    }

    fun isSandhi(phonologicalRule: PhonologicalRule): Boolean {
        val isLanguageStressRuleBakeable = language.stressType in listOf(StressType.None, StressType.NotFixed)
        val hasStressRules = phonologicalRule.matchers.any { m -> m.any { it is ProsodyMatcher && Prosody.Stress in it.prosody }}

        if (!isLanguageStressRuleBakeable && hasStressRules)
            return true

        val isStartBoundary = phonologicalRule.precedingMatchers.firstOrNull() == BorderPhonemeMatcher
        if (isStartBoundary && language.changeParadigm.wordChangeParadigm.hasPrefixes())
            return true

        val isEndBoundary = phonologicalRule.followingMatchers.lastOrNull() == BorderPhonemeMatcher
        if (isEndBoundary && language.changeParadigm.wordChangeParadigm.hasSuffixes())
            return true

        return false
    }
}
