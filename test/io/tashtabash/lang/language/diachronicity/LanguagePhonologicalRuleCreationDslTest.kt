package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.util.makeDefLang
import io.tashtabash.lang.language.util.testPhonemeContainer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class LanguagePhonologicalRuleCreationDslTest {
    @Test
    fun `escapeStress() correctly modifies matchers`() {
        val language = makeDefLang(listOf())
            .copy(stressType = StressType.NotFixed)

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                createRule("(o{-Stress}) -> - / _ ")
            },
            testPhonemeContainer.createPhonologicalRulesFor(language) {
                createRule("${escapeStress("o")} -> - / _ ")
            }
        )
    }

    @Test
    fun `escapeStress() correctly modifies complex matchers`() {
        val language = makeDefLang(listOf())
            .copy(stressType = StressType.NotFixed)

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                createRule("(V[-Long]{-Stress}) -> - / _ ")
            },
            testPhonemeContainer.createPhonologicalRulesFor(language) {
                createRule("${escapeStress("(V[-Long])")} -> - / _ ")
            }
        )
    }

    @Test
    fun `createRules() substitutes a template for a correct matcher`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createRules("<[+Labialized]> -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("o -> [-Labialized] / _ $"),
                    createRule("|oo| -> [-Labialized] / _ $"),
                    createRule("u -> [-Labialized] / _ $"),
                    createRule("|uu| -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createRules() substitutes a template for a correct complex matcher`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createRules("<([+Labialized][+Close])> -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("u -> [-Labialized] / _ $"),
                    createRule("|uu| -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createRules() substitutes a template for a correct matcher inside complex matchers`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createRules("(<[+Labialized]>{-Stress}) -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("(o{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|oo|{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(u{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|uu|{-Stress}) -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createRules() can interact with escapeStress()`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createRules("${escapeStress("<[+Labialized]>")} -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("(o{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|oo|{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(u{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|uu|{-Stress}) -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createWeakRules() can parse templates`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createWeakRules("${escapeStress("<([+Labialized][+Close])>")} -> [-Labialized] / _ ")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("(u{-Stress}) -> [-Labialized] / _ "),
                    createRule("(u{-Stress}) -> [-Labialized] / $ _ "),
                    createRule("(u{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|uu|{-Stress}) -> [-Labialized] / _ "),
                    createRule("(|uu|{-Stress}) -> [-Labialized] / $ _ "),
                    createRule("(|uu|{-Stress}) -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }
}
