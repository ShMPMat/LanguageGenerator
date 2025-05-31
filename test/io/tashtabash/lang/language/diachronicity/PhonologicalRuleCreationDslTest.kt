package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.util.testPhonemeContainer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class PhonologicalRuleCreationDslTest {
    @Test
    fun `createWeakRules() correctly adds bounds`() {
        val result = testPhonemeContainer.createPhonologicalRules {
            createWeakRules("a -> o / _")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("a -> o / _"),
                    createRule("a -> o / $ _"),
                    createRule("a -> o / _ $")
                )
            },
            result
        )
    }

    @Test
    fun `createWeakRules() correctly rewrites present preceding and following matchers`() {
        val result = testPhonemeContainer.createPhonologicalRules {
            createWeakRules("a -> o / C _ C")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("a -> o / C _ C"),
                    createRule("a -> o / $ _ C"),
                    createRule("a -> o / C _ $")
                )
            },
            result
        )
    }

    @Test
    fun `allowSyllableStructureChange is applied to rules`() {
        val result = testPhonemeContainer.createPhonologicalRules {
            val noSyllableStructureChange = createWeakRules("a -> o / _") + createRule("a -> u / _")
            allowSyllableStructureChange = true
            val syllableStructureChange = createWeakRules("a -> o / _") + createRule("a -> u / _")
            allowSyllableStructureChange = false
            noSyllableStructureChange + syllableStructureChange + createRule("o -> u / _")
        }

        assertEquals(
            testPhonemeContainer.createPhonologicalRules {
                listOf(
                    createRule("a -> o / _"),
                    createRule("a -> o / $ _"),
                    createRule("a -> o / _ $"),
                    createRule("a -> u / _ "),
                    createRule("a -> o / _").copy(allowSyllableStructureChange = true),
                    createRule("a -> o / $ _").copy(allowSyllableStructureChange = true),
                    createRule("a -> o / _ $").copy(allowSyllableStructureChange = true),
                    createRule("a -> u / _ ").copy(allowSyllableStructureChange = true),
                    createRule("o -> u / _ ")
                )
            },
            result
        )
    }
}
