package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeModifier.*
import io.tashtabash.lang.language.phonology.PhonemeType.*
import io.tashtabash.lang.language.util.testPhonemeContainer
import io.tashtabash.lang.utils.composeUniquePairs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


internal class PhonemeMatcherTest {
    @ParameterizedTest(name = "{0} combined with {1} = {1} combined with {0}")
    @MethodSource("phonemeMatcherProvider")
    fun `times method is commutative`(first: PhonemeMatcher, second: PhonemeMatcher) {
        assertEquals(
            first * second,
            second * first
        )
    }

    @Test
    fun `MulMatcher times MulMatcher filters out duplicate matchers`() {
        val matcher = MulMatcher(TypePhonemeMatcher(Consonant), AbsentModifierPhonemeMatcher(PhonemeModifier.Long))

        assertEquals(matcher * matcher, matcher)
    }

    @Test
    fun `MulMatcher times MulMatcher merges ModifierPhonemeMatcher if possible`() {
        assertEquals(
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentModifierPhonemeMatcher(PhonemeModifier.Long))
                    * AbsentModifierPhonemeMatcher(Voiced),
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentModifierPhonemeMatcher(PhonemeModifier.Long, Voiced))
        )
    }

    companion object {
        @JvmStatic
        fun phonemeMatcherProvider(): Stream<Arguments> {
            val matchers = listOf(
                PassingPhonemeMatcher,
                BorderPhonemeMatcher,
                ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("a")),
                ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("o")),
                ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("b")),
                ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("t")),
                TypePhonemeMatcher(Vowel),
                TypePhonemeMatcher(Consonant),
                ModifierPhonemeMatcher(Labialized),
                ModifierPhonemeMatcher(Nasalized, PhonemeModifier.Long),
                ModifierPhonemeMatcher(Nasalized),
                ModifierPhonemeMatcher(Palatilized),
                AbsentModifierPhonemeMatcher(Labialized),
                AbsentModifierPhonemeMatcher(Nasalized, PhonemeModifier.Long),
                AbsentModifierPhonemeMatcher(Nasalized),
                AbsentModifierPhonemeMatcher(Palatilized),
                MulMatcher(TypePhonemeMatcher(Consonant), AbsentModifierPhonemeMatcher(PhonemeModifier.Long)),
                MulMatcher(TypePhonemeMatcher(Vowel), AbsentModifierPhonemeMatcher(Nasalized, Labialized)),
            )

            return composeUniquePairs(matchers, matchers)
                .map { (f, s) -> Arguments.of(f, s) }
                .stream()
        }
    }
}