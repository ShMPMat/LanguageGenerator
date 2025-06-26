package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.random.singleton.randomUnwrappedElementOrNull
import io.tashtabash.random.toSampleSpaceObject


abstract class UnionPhonologicalChangeTendency : AbstractPhonologicalChangeTendency() {
    abstract val tendencies: List<PhonologicalChangeTendency>

    override fun computeDevelopmentChance(language: Language): Double =
        tendencies.sumOf { it.computeDevelopmentChance(language) }

    override fun computeRetentionChance(language: Language): Double {
        // Prob that none of the tendencies will be retained
        val probNoneRetained = tendencies.map { 1.0 - it.computeRetentionChance(language) }
            .reduce(Double::times)

        return 1.0 - probNoneRetained
    }

    override fun getRule(language: Language, phonemeContainer: PhonemeContainer): PhonologicalRule? =
         tendencies.map { it.toSampleSpaceObject(it.computeDevelopmentChance(language)) }
            .randomUnwrappedElementOrNull()
            ?.getRule(language, phonemeContainer)
}
