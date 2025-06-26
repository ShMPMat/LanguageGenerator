package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.language.Language


abstract class CacheablePhonologicalChangeTendency: DefaultPhonologicalChangeTendency() {
    // This is a heuristic. The class assumes, that nothing else changes the development condition
    // for the language, so it's ok to cache the value until computeDevelopmentChance is called
    private var cachedDevelopmentChance: Double? = null

    // The method assumes that it is called on the same language changed with time
    override fun computeDevelopmentChance(language: Language): Double =
        cachedDevelopmentChance ?:
        internalComputeDevelopmentChance(language).also {
            cachedDevelopmentChance = it
        }

    protected abstract fun internalComputeDevelopmentChance(language: Language): Double
}
