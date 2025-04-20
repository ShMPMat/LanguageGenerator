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
    fun `times() applies changes to an epenthesis`() {
        assertEquals(
            listOf(createTestPhonologicalRule("V -> _(d)(n) / $ _ a")),
            createTestPhonologicalRule("V -> _(d)(r) / $ _ a") * createTestPhonologicalRule("r -> n / _ ")
        )
    }

    @Test
    fun `times() apply changes if there's a matching epenthesis between matchers`() {
        assertEquals(
            listOf(createTestPhonologicalRule("V(C[-Voiced]) -> _(t)_ / _ ")),
            createTestPhonologicalRule("V(C[-Voiced]) -> _(d)_ / _ ") * createTestPhonologicalRule("V(C[+Voiced]) -> _[-Voiced] / _ ")
        )
    }

    @Test
    fun `times() deletes an epenthesis if it matches`() {
        assertEquals(
            listOf(createTestPhonologicalRule("VV -> __ / _ ")),
            createTestPhonologicalRule("VV -> _(d)_ / _ ") * createTestPhonologicalRule("d -> - / _ ")
        )
    }

    @Test
    fun `times() keeps the original rules if it's narrowed on a passing matcher`() {
        assertEquals(
            listOf(createTestPhonologicalRule("a* -> o_ / _ "), createTestPhonologicalRule("oC -> u_ / _ ")),
            createTestPhonologicalRule("a* -> o_ / _ ") * createTestPhonologicalRule("oC -> u_ / _ ")
        )
    }

    @Test
    fun `times() deletes epenthesis if it matches`() {
        assertEquals(
            listOf(createTestPhonologicalRule("VV -> __ / _ ")),
            createTestPhonologicalRule("VV -> _(d)_ / _ ") * createTestPhonologicalRule("d -> - / _ ")
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
    fun `plus() applies the rule on the both borders simultaneously`() {
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
    fun `plus() applies the rule on the both borders simultaneously if the applications do not intersect`() {
        assertEquals(
            listOf(
                createTestPhonologicalRule("VCouCa -> __ia_i / _ "),
                createTestPhonologicalRule("ouCa -> aa_i / _ "),
                createTestPhonologicalRule("VCou -> __ia /  _ "),
                createTestPhonologicalRule("ou -> aa /  _ ")
            ),
            createTestPhonologicalRule("ou -> aa /  _ ") + createTestPhonologicalRule("VCa -> __i /  _ ")
        )
    }

    @Test
    fun `plus() merges substitutions after deletions correctly`() {
        assertEquals(
            listOf(
                createTestPhonologicalRule("ou -> -u /  _ ")
            ),
            createTestPhonologicalRule("ou -> -a /  _ ") + createTestPhonologicalRule("a -> u /  _ ")
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

    @Test
    fun `plus() returns the first rule if the second makes doesn't change the original matchers`() {
        assertEquals(
            listOf(createTestPhonologicalRule("o -> a / C _ C")),
            createTestPhonologicalRule("o -> a / C _ C") + createTestPhonologicalRule("b -> t / _ ")
        )
    }

    @Test
    fun `plus() returns the first rule if the second makes it longer without changing the original matchers & substitutions`() {
        assertEquals(
            listOf(createTestPhonologicalRule("o -> a / C _ C")),
            createTestPhonologicalRule("o -> a / C _ C") + createTestPhonologicalRule("b -> t / i _ ")
        )
    }

    @Test
    fun `plus() applies changes to an epenthesis`() {
        assertEquals(
            listOf(createTestPhonologicalRule("V -> _(d)(n) / $ _ a")),
            createTestPhonologicalRule("V -> _(d)(r) / $ _ a") + createTestPhonologicalRule("r -> n / _ ")
        )
    }

    @Test
    fun `plus() apply changes if there's a matching epenthesis between matchers`() {
        assertEquals(
            listOf(createTestPhonologicalRule("V(C[-Voiced]) -> _(t)_ / _ ")),
            createTestPhonologicalRule("V(C[-Voiced]) -> _(d)_ / _ ") + createTestPhonologicalRule("V(C[+Voiced]) -> _[-Voiced] / _ ")
        )
    }

    @Test
    fun `plus() deletes an epenthesis if it matches`() {
        assertEquals(
            listOf(createTestPhonologicalRule("VV -> __ / _ ")),
            createTestPhonologicalRule("VV -> _(d)_ / _ ") + createTestPhonologicalRule("d -> - / _ ")
        )
    }

    @Test
    fun `plus() keeps the original rule if it's narrowed on a passing matcher`() {
        assertEquals(
            listOf(createTestPhonologicalRule("aC -> u_ / _ "), createTestPhonologicalRule("a -> o / _ *")),
            createTestPhonologicalRule("a -> o / _ *") + createTestPhonologicalRule("oC -> u_ / _ ")
        )
    }
}
