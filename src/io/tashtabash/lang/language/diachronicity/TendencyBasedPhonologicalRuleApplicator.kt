package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.tendency.PhonologicalChangeTendency
import io.tashtabash.lang.language.diachronicity.tendency.createDefaultPhonologicalChangeTendencies
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.random.singleton.testProbability


class TendencyBasedPhonologicalRuleApplicator(
    private val phonemeContainer: PhonemeContainer,
    private val possibleTendencies: MutableList<PhonologicalChangeTendency> =
        createDefaultPhonologicalChangeTendencies().toMutableList(),
    private val tendencyStickiness: Double = 1.0,
    private val tendencyDevelopmentChance: Double = 0.2,
    private val randomRuleNarrowingProbability: Double = 0.8
) {
    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    private var tendencies = mutableListOf<PhonologicalChangeTendency>()

    fun applyPhonologicalRule(language: Language, rulesContainer: PhonologicalRulesContainer): Language {
        developTendency(language)
        val shiftedLanguage = applyRules(language, rulesContainer)
        abandonTendencies(language)

        return shiftedLanguage
    }

    private fun applyRules(language: Language, rulesContainer: PhonologicalRulesContainer): Language {
        var shiftedLanguage = language

        for (tendency in tendencies) {
            val rule = tendency.getRule(language, phonemeContainer)
            if (rule == null) {
                _messages += "Tendency ${tendency.name} didn't cause any changes"
                continue
            }

            val applicator = PhonologicalRuleApplicator()
            shiftedLanguage = applicator.applyPhonologicalRule(language, rule)
            _messages += "Tendency ${tendency.name} caused $rule"
            _messages += applicator.messages
        }

        if (tendencies.isEmpty()) {
            _messages += "No tendencies, applying a random rule"
            val randomApplicator = RandomPhonologicalRuleApplicator(randomRuleNarrowingProbability)
            shiftedLanguage = randomApplicator.applyRandomPhonologicalRule(language, rulesContainer)
            _messages += randomApplicator.messages
        }

        return shiftedLanguage
    }

    private fun developTendency(language: Language) {
        tendencyDevelopmentChance.chanceOf {
            val tendency = possibleTendencies.filter { it !in tendencies }
                .randomElementOrNull { it.computeDevelopmentChance(language) }
                ?: return
            tendencies += tendency

            _messages += "A new tendency is developed: ${tendencies.last().name}"
        }
    }

    private fun abandonTendencies(language: Language) {
        tendencies.removeIf { !shouldRetainTendency(it, language) }
    }

    private fun shouldRetainTendency(tendency: PhonologicalChangeTendency, language: Language): Boolean {
        val retentionProbability = tendency.computeRetentionChance(language) * tendencyStickiness
        val shouldRetain = retentionProbability.testProbability()

        if (!shouldRetain) {
            possibleTendencies.removeIf { it.name == tendency.name }
            possibleTendencies += tendency.getNewInstance()

            _messages += "Tendency ${tendency.name} is abandoned"
        }

        return shouldRetain
    }
}
