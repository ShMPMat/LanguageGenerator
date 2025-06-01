package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.morphem.change.substitution.createPhonemeSubstitutions
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.matcher.BorderPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers
import io.tashtabash.lang.language.phonology.prosody.StressType


fun createPhonologicalRule(rule: String, phonemeContainer: PhonemeContainer): PhonologicalRule {
    val allowSyllableStructureChange = rule.last() == '!'
    val clearedRule = rule.dropLastWhile { it == '!' }

    return PhonologicalRule(
        clearedRule.dropWhile { it != '/' }
            .drop(1)
            .dropLastWhile { it != '_' }
            .dropLast(1)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropLastWhile { it != '>' }
            .dropLast(2)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropWhile { it != '/' }
            .dropWhile { it != '_' }
            .drop(1)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropWhile { it != '>' }
            .drop(1)
            .dropLastWhile { it != '/' }
            .dropLast(1)
            .replace(" ", "")
            .let { createPhonemeSubstitutions(it, phonemeContainer) },
        allowSyllableStructureChange
    )
}


open class PhonologicalRuleCreationDsl(val phonemeContainer: PhonemeContainer) {
    var allowSyllableStructureChange = false

    fun createRule(rule: String): PhonologicalRule =
        createPhonologicalRule(rule, phonemeContainer)
            .copy(allowSyllableStructureChange = allowSyllableStructureChange)

    fun createWeakRules(rule: PhonologicalRule): List<PhonologicalRule> = listOf(
        rule,
        rule.copy(precedingMatchers = listOf(BorderPhonemeMatcher)),
        rule.copy(followingMatchers = listOf(BorderPhonemeMatcher))
    )

    open fun createWeakRules(rule: String): List<PhonologicalRule> {
        val baseRule = createRule(rule)

        return createWeakRules(baseRule)
    }

    fun escape(phoneme: Phoneme): String =
        escape(phoneme.symbol)

    fun escape(symbol: String): String =
        if (symbol.length != 1)
            "|$symbol|"
        else
            symbol
}

class LanguagePhonologicalRuleCreationDsl(
    phonemeContainer: PhonemeContainer,
    val language: Language
): PhonologicalRuleCreationDsl(phonemeContainer) {
    fun escapeStress(symbol: String): String =
        if (language.stressType == StressType.None)
            escape(symbol)
        else if (symbol.first() == '(' && symbol.last() == ')')
            symbol.dropLast(1) + "{-Stress})"
        else if (symbol.first() == '<' && symbol.last() == '>')
            // The template creator will take care of escaping
            "($symbol{-Stress})"
        else
            "(${escape(symbol)}{-Stress})"

    fun escapeStress(phoneme: Phoneme): String =
        escapeStress(phoneme.symbol)

    fun createRules(templateRule: String): List<PhonologicalRule> {
        if ('<' !in templateRule)
            return listOf(createRule(templateRule))

        val templateRegex = Regex("<(.*?)>")
        val matchResult = templateRegex.find(templateRule)!!
        val matcherString = matchResult.groupValues[1]
        val matcher = createPhonemeMatcher(matcherString, phonemeContainer)
        val matchedPhonemes = language.phonemeContainer.getPhonemes(matcher)

        return matchedPhonemes.flatMap { createRules(templateRule.replaceRange(matchResult.range, escape(it))) }
    }

    override fun createWeakRules(rule: String): List<PhonologicalRule> {
        // Parse templates if present
        val baseRules = createRules(rule)

        return baseRules.flatMap { createWeakRules(it) }
    }
}

inline fun <T> PhonemeContainer.createPhonologicalRules(command: PhonologicalRuleCreationDsl.() -> T) =
    PhonologicalRuleCreationDsl(this)
        .run(command)

inline fun <T> PhonemeContainer.createPhonologicalRulesFor(
    language: Language,
    command: LanguagePhonologicalRuleCreationDsl.() -> T
) =
    LanguagePhonologicalRuleCreationDsl(this, language)
        .run(command)
