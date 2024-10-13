package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.Language
import io.tashtabash.random.singleton.randomElementOrNull


class RandomPhonologicalRuleApplicator {
    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    fun applyRandomPhonologicalRule(language: Language, rulesContainer: PhonologicalRulesContainer): Language {
        val phonologicalRule = rulesContainer
            .getApplicableRules(language)
            .randomElementOrNull()
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
}
