package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.grammar.RandomGrammaticalChangeApplicator
import io.tashtabash.random.singleton.randomUnwrappedElement
import io.tashtabash.random.withProb
import java.rmi.UnexpectedException


class DiachronicChangeApplicator(phonemeContainer: PhonemeContainer, val rulesContainer: PhonologicalRulesContainer) {
    private val weightedApplicators = listOf(
        TendencyBasedPhonologicalRuleApplicator(phonemeContainer) withProb 1.0,
        RandomGrammaticalChangeApplicator() withProb .8,
    )

    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages
    private var phonologicalMessagesSize = 0
    private var grammarMessagesSize = 0

    fun apply(language: Language): Language =
        when (val applicator = weightedApplicators.randomUnwrappedElement()) {
            is TendencyBasedPhonologicalRuleApplicator -> applicator.applyPhonologicalRule(language, rulesContainer).also {
                _messages += applicator.messages.drop(phonologicalMessagesSize)
                phonologicalMessagesSize = applicator.messages.size
            }
            is RandomGrammaticalChangeApplicator -> applicator.apply(language).also {
                _messages += applicator.messages.drop(grammarMessagesSize)
                grammarMessagesSize = applicator.messages.size
            }
            else -> throw UnexpectedException("Unexpected applicator ${applicator.javaClass}")
        }
}
