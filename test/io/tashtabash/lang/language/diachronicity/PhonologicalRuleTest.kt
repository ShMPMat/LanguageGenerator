package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.util.createTestPhonologicalRule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class PhonologicalRuleTest {
    @Test
    fun `times() changes a single vowel`() {
        assertEquals(
            listOf(createTestPhonologicalRule("aC -> u_ / $ _ ")),
            createTestPhonologicalRule("aC -> |ii|_ / $ _ ") * createTestPhonologicalRule("|ii| -> u / _ ")
        )
    }

    @Test
    fun `times() correctly handles ModifierPhonemeSubstitution`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C[+Labialized] -> _i / $ _ ")),
            createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ") * createTestPhonologicalRule("V -> i / _ ")
        )
    }

    @Test
    fun `times() returns the original rules if the combination of a PhonemeModifierMatcher with another rule results in a narrowing`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ "), createTestPhonologicalRule("ɯ -> i / _ ")),
            createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ") * createTestPhonologicalRule("ɯ -> i / _ ")
        )
    }

    @Test
    fun `times() returns the original rules if the combination of 2 PhonemeModifierMatchers results in a narrowing`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ "), createTestPhonologicalRule("(V[-Long]) -> [+Long] / C _ ")),
            createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ") * createTestPhonologicalRule("(V[-Long]) -> [+Long] / C _ ")
        )
    }

    @Test
    fun `times() returns a single rule if there's no narrowing from a PhonemeModifierMatcher`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C([+Labialized][-Long]) -> _[+Long,-Labialized] / $ _ ")),
            createTestPhonologicalRule("C([+Labialized][-Long]) -> _[-Labialized] / $ _ ") * createTestPhonologicalRule("(V[-Long]) -> [+Long] / C _ ")
        )
    }

    @Test
    fun `times() correctly handles EpenthesisPhonemeSubstitution`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C -> _(a) / $ _ C")),
            createTestPhonologicalRule("C -> _(o) / $ _ C") * createTestPhonologicalRule("o -> a / _ ")
        )
    }

    @Test
    fun `times() correctly merges changes if the second one is larger`() {
        assertEquals(
            listOf(createTestPhonologicalRule("o -> a / $ _ "), createTestPhonologicalRule("aCa -> i_u / $ _ ")),
            createTestPhonologicalRule("o -> a / $ _ ") * createTestPhonologicalRule("aCa -> i_u / $ _ ")
        )
    }

    @Test
    fun `times() returns the original rules for complicated cases with multiple partially overlapping cases`() {
        assertEquals(
            listOf(createTestPhonologicalRule("o -> a / _ "), createTestPhonologicalRule("aCa -> i_u / _ ")),
            createTestPhonologicalRule("o -> a / _ ") * createTestPhonologicalRule("aCa -> i_u / _ ")
        )
    }

    @Test
    fun `times() returns the first rule if the second isn't applicable`() {
        assertEquals(
            listOf(createTestPhonologicalRule("o -> a / _ ")),
            createTestPhonologicalRule("o -> a / _ ") * createTestPhonologicalRule("i -> a / _ ")
        )
    }

    @Test
    fun `times() returns the first rule if the second isn't applicable (the longer case)`() {
        assertEquals(
            listOf(createTestPhonologicalRule("aC -> u_ / $ _ ")),
            createTestPhonologicalRule("aC -> u_ / $ _ ") * createTestPhonologicalRule("a -> i / _ ")
        )
    }

    @Test
    fun `plus() changes a single vowel`() {
        assertEquals(
            listOf(createTestPhonologicalRule("aC -> u_ / $ _ ")),
            createTestPhonologicalRule("aC -> |ii|_ / $ _ ") + createTestPhonologicalRule("|ii| -> u / _ ")
        )
    }

    @Test
    fun `plus() correctly handles ModifierPhonemeSubstitution`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C[+Labialized] -> _i / $ _ ")),
            createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ") + createTestPhonologicalRule("V -> i / _ ")
        )
    }

    @Test
    fun `plus() returns the narrowed and the original rule if the combination of a PhonemeModifierMatcher with another rule results in a narrowing`() {
        assertEquals(
            listOf(createTestPhonologicalRule("Cu -> _i / $ _ "), createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ")),
            createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ") + createTestPhonologicalRule("ɯ -> i / _ ")
        )
    }

    @Test
    fun `plus() returns the narrowed and the original rule if the combination of 2 PhonemeModifierMatchers results in a narrowing`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C[+Labialized] -> _[-Labialized,+Long] / $ _ "), createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ")),
            createTestPhonologicalRule("C[+Labialized] -> _[-Labialized] / $ _ ") + createTestPhonologicalRule("(V[-Long]) -> [+Long] / C _ ")
        )
    }

    @Test
    fun `plus() returns a single rule if there's no narrowing from a PhonemeModifierMatcher`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C([+Labialized][-Long]) -> _[+Long,-Labialized] / $ _ ")),
            createTestPhonologicalRule("C([+Labialized][-Long]) -> _[-Labialized] / $ _ ") + createTestPhonologicalRule("(V[-Long]) -> [+Long] / C _ ")
        )
    }

    @Test
    fun `plus() correctly handles EpenthesisPhonemeSubstitution`() {
        assertEquals(
            listOf(createTestPhonologicalRule("C -> _(a) / $ _ C")),
            createTestPhonologicalRule("C -> _(o) / $ _ C") + createTestPhonologicalRule("o -> a / _ ")
        )
    }

    @Test
    fun `plus() correctly merges changes if the second one is larger`() {
        assertEquals(
            listOf(createTestPhonologicalRule("oCa -> i_u / $ _ "), createTestPhonologicalRule("o -> a / $ _ ")),
            createTestPhonologicalRule("o -> a / $ _ ") + createTestPhonologicalRule("aCa -> i_u / $ _ ")
        )
    }

    @Test
    fun `plus() merges changes if the second rule applies on the both borders`() {
        assertEquals(
            listOf(
                createTestPhonologicalRule("oCa -> i_u / _ "),
                createTestPhonologicalRule("aCo -> i_u / _ "),
                createTestPhonologicalRule("o -> a / _ ")
            ),
            createTestPhonologicalRule("o -> a /  _ ") + createTestPhonologicalRule("aCa -> i_u /  _ ")
        )
    }

    @Test
    fun `plus() merges changes if the second rule applies on the both borders simultaneously`() {
        assertEquals(
            listOf(
                createTestPhonologicalRule("VCoCa -> _-a-a / _ "),
                createTestPhonologicalRule("oCa -> a-a / _ "),
                createTestPhonologicalRule("VCo -> _-a / _ "),
                createTestPhonologicalRule("o -> a / _ ")
            ),
            createTestPhonologicalRule("o -> a /  _ ") + createTestPhonologicalRule("VCa -> _-a /  _ ")
        )
    }

    @Test
    fun `plus() returns the first rule if the second isn't applicable`() {
        assertEquals(
            listOf(createTestPhonologicalRule("o -> a / _ ")),
            createTestPhonologicalRule("o -> a / _ ") + createTestPhonologicalRule("i -> a / _ ")
        )
    }

    @Test
    fun `plus() returns the first rule if the second isn't applicable (the longer case)`() {
        assertEquals(
            listOf(createTestPhonologicalRule("aC -> u_ / $ _ ")),
            createTestPhonologicalRule("aC -> u_ / $ _ ") + createTestPhonologicalRule("a -> i / _ ")
        )
    }
}
