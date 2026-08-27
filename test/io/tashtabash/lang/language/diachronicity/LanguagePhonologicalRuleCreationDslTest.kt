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
            createRules("<(V[+Labialized][-Nasalized])> -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("o -> [-Labialized] / _ $"),
                    createRule("u -> [-Labialized] / _ $"),
                    createRule("|o:| -> [-Labialized] / _ $"),
                    createRule("|u:| -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createRules() substitutes a template for a correct complex matcher`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createRules("<(V[+Labialized][+Close][-Nasalized])> -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("u -> [-Labialized] / _ $"),
                    createRule("|u:| -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createRules() substitutes a template for a correct matcher inside complex matchers`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createRules("(<(V[+Labialized][-Nasalized])>{-Stress}) -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("(o{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(u{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|o:|{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|u:|{-Stress}) -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createRules() can interact with escapeStress()`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createRules("${escapeStress("<(V[+Labialized][-Nasalized])>")} -> [-Labialized] / _ $")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("(o{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(u{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|o:|{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|u:|{-Stress}) -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }

    @Test
    fun `createWeakRules() can parse templates`() {
        val language = makeDefLang(listOf())

        val result = testPhonemeContainer.createPhonologicalRulesFor(language) {
            createWeakRules("${escapeStress("<(V[+Labialized][+Close][-Nasalized])>")} -> [-Labialized] / _ ")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("(u{-Stress}) -> [-Labialized] / _ "),
                    createRule("(u{-Stress}) -> [-Labialized] / $ _ "),
                    createRule("(u{-Stress}) -> [-Labialized] / _ $"),
                    createRule("(|u:|{-Stress}) -> [-Labialized] / _ "),
                    createRule("(|u:|{-Stress}) -> [-Labialized] / $ _ "),
                    createRule("(|u:|{-Stress}) -> [-Labialized] / _ $"),
                )
            },
            result
        )
    }
}
