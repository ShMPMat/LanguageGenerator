package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.singleton.randomUnwrappedElementOrNull


abstract class OptionListPhonologicalChangeTendency : AbstractPhonologicalChangeTendency() {
    protected val appliedRules = mutableListOf<PhonologicalRule>()

    override fun getRule(language: Language, phonemeContainer: PhonemeContainer): PhonologicalRule? =
        getOptions(language, phonemeContainer)
            .filter { it.value !in appliedRules }
            .randomUnwrappedElementOrNull()
            ?.also { appliedRules += it }

    protected abstract fun getOptions(
        language: Language,
        phonemes: PhonemeContainer
    ): List<GenericSSO<PhonologicalRule>>
}
