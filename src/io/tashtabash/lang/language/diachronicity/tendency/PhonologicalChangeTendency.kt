package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.PhonologicalRule


interface PhonologicalChangeTendency {
    val name: String

    fun computeDevelopmentChance(language: Language): Double

    fun computeRetentionChance(language: Language): Double

    fun getRule(language: Language, phonemeContainer: PhonemeContainer): PhonologicalRule?

    fun getNewInstance(): PhonologicalChangeTendency
}


fun createDefaultPhonologicalChangeTendencies() = listOf(
    VowelReduction(), ShortVowelReduction(), VowelShortening(), VowelCentralization(),
    VowelMerge(),
    Voicing(), Devoicing(),
    RegressiveVoicingAssimilation(), ProgressiveVoicingAssimilation(),
    RegressiveNasalization(), ProgressiveNasalization(),
    IntervocalicConsonantInsertion(), InterconsonanticVowelInsertion(), EndConsonantDrop(),
    SyllableSimplification()
)
