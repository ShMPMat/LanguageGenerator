package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.language.Language


abstract class DefaultPhonologicalChangeTendency : OptionListPhonologicalChangeTendency() {
    protected open val defaultRetentionChance = 0.9

    override fun computeRetentionChance(language: Language): Double =
        if (appliedRules.isNotEmpty())
            defaultRetentionChance
        else
            1.0
}
