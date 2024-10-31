package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.matcher.*
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.random.singleton.testProbability


class RandomPhonologicalRuleApplicator(private val narrowingProbability: Double = 0.8) {
    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    fun applyRandomPhonologicalRule(language: Language, rulesContainer: PhonologicalRulesContainer): Language {
        val phonologicalRule = chooseRule(language, rulesContainer)
        if (phonologicalRule == null) {
            _messages += "No changes available for the language"
            return language
        }
        _messages += "Applying rule '$phonologicalRule'"

        val ruleApplicator = PhonologicalRuleApplicator()
        val shiftedLanguage = ruleApplicator.applyPhonologicalRule(language, phonologicalRule)
        _messages += ruleApplicator.messages

        return shiftedLanguage
    }

    fun chooseRule(language: Language, rulesContainer: PhonologicalRulesContainer): PhonologicalRule? {
        var rule = rulesContainer
            .getApplicableRules(language)
            .randomElementOrNull()
            ?: return null

        while (!isRuleExact(rule) && narrowingProbability.testProbability())
            rule = narrowRule(rule, language)
                ?: break

        return rule
    }

    // Returns true is there exists no different rule which is a narrower case of this rule
    private fun isRuleExact(rule: PhonologicalRule): Boolean =
        rule.matchers.all {
            isMatcherExact(it)
        }

    // Returns true is there exists no different matcher which is a narrower case of this matcher
    private fun isMatcherExact(matcher: PhonemeMatcher): Boolean = when (matcher) {
        is TypePhonemeMatcher, PassingPhonemeMatcher -> false
        is ModifierPhonemeMatcher -> false
        is AbsentModifierPhonemeMatcher -> false
        is MulMatcher -> false
        else -> true
    }

    // Returns null if the rule wasn't narrowed.
    private fun narrowRule(rule: PhonologicalRule, language: Language): PhonologicalRule? {
        val (i, newMatcher) = rule.matchers
            .mapIndexedNotNull { i, matcher -> if (isMatcherExact(matcher)) null else i to matcher }
            .randomElementOrNull()
            ?.let { (i, matcher) -> i to narrowMatcher(matcher, language) }
            ?: return null
        val newMatchers = rule.matchers.toMutableList()
        newMatchers[i] = newMatcher

        return PhonologicalRule(
            newMatchers,
            rule.precedingMatchers.size,
            rule.followingMatchers.size,
            rule.resultingPhonemes,
            rule.allowSyllableStructureChange
        )
    }

    private fun narrowMatcher(matcher: PhonemeMatcher, language: Language): PhonemeMatcher = when (matcher) {
        is TypePhonemeMatcher ->
            if (0.5.testProbability()) {
                val randomModifier = PhonemeModifier.values(matcher.phonemeType)
                    .randomElement()

                language.phonemeContainer
                    .getPhonemes(setOf(randomModifier))
                    .filter { it.type == matcher.phonemeType }
                    .takeIf { it.isNotEmpty() }
                    ?.let { MulMatcher(matcher, ModifierPhonemeMatcher(randomModifier)) }
                    ?: matcher
            } else {
                val randomModifier = PhonemeModifier.values(matcher.phonemeType)
                    .randomElement()

                language.phonemeContainer
                    .getPhonemesNot(setOf(randomModifier))
                    .filter { it.type == matcher.phonemeType }
                    .takeIf { it.isNotEmpty() }
                    ?.let { MulMatcher(matcher, AbsentModifierPhonemeMatcher(randomModifier)) }
                    ?: matcher
            }
        is PassingPhonemeMatcher -> PhonemeType.values()
            .asList()
            .randomElement()
            .let { TypePhonemeMatcher(it) }
        is ModifierPhonemeMatcher -> language.phonemeContainer
            .getPhonemes(matcher.modifiers)
            .randomElementOrNull()
            ?.let { ExactPhonemeMatcher(it) }
            ?: matcher
        is AbsentModifierPhonemeMatcher -> language.phonemeContainer
            .getPhonemesNot(matcher.modifiers)
            .randomElementOrNull()
            ?.let { ExactPhonemeMatcher(it) }
            ?: matcher
        is MulMatcher -> {
            matcher.matchers
                .mapNotNull { subMatcher -> if (isMatcherExact(subMatcher)) null else subMatcher  }
                .randomElementOrNull()
                ?.let { subMatcher ->
                    val narrowedMatcher = narrowMatcher(subMatcher, language)

                    matcher * narrowedMatcher
                }
                ?: matcher
        }
        else -> throw LanguageException("Can't narrow down $matcher")
    }
        .takeIf { isMatcherApplicable(it, language) }
        ?: matcher
}
