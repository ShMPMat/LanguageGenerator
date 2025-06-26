package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.diachronicity.getChangingPhonemes
import io.tashtabash.lang.language.phonology.matcher.BorderPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.matchAll


abstract class MatcherConditionPhonologicalChangeTendency : CacheablePhonologicalChangeTendency() {
    abstract fun createMatchers(phonemeContainer: PhonemeContainer): List<List<PhonemeMatcher>>

    open val divisionCoefficient: Double = 10000.0

    override fun internalComputeDevelopmentChance(language: Language): Double {
        val matchers = createMatchers(language.phonemeContainer)
        val matchNumber = matchers.sumOf { it.countMatchesUnique(language) }

        return matchNumber / divisionCoefficient
    }
}

fun List<PhonemeMatcher>.countMatches(language: Language) =
    language.changeParadigm.wordChangeParadigm.getAllWordForms(language.lexis, true)
        .flatMap { (words) -> words.words }
        .count { word ->
            val changingPhonemes = getChangingPhonemes(
                word,
                this.first() == BorderPhonemeMatcher,
                this.last() == BorderPhonemeMatcher
            )
            this.matchAll(changingPhonemes)
        }

fun List<PhonemeMatcher>.countMatchesUnique(language: Language) =
    language.changeParadigm.wordChangeParadigm.getUniqueWordForms(language.lexis, true)
        .count { word ->
            val changingPhonemes = getChangingPhonemes(
                word,
                this.first() == BorderPhonemeMatcher,
                this.last() == BorderPhonemeMatcher
            )
            this.matchAll(changingPhonemes)
        }
