package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.phonology.ArticulationManner
import io.tashtabash.lang.language.phonology.ArticulationPlace
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeModifier.*
import io.tashtabash.lang.language.phonology.PhonemeType.*
import io.tashtabash.lang.language.phonology.prosody.Prosody
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
        val matcher = MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long))

        assertEquals(matcher * matcher, matcher)
    }

    @Test
    fun `MulMatcher times MulMatcher merges ModifierPhonemeMatcher if possible`() {
        assertEquals(
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long))
                    * AbsentCharacteristicPhonemeMatcher(Voiced),
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long, Voiced))
        )
    }

    @Test
    fun `MulMatcher times ExactPhonemeMatcher results in ExactPhonemeMatcher if the merge is possible`() {
        assertEquals(
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(Voiced))
                    * ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("t")),
            ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("t"))
        )
    }

    @Test
    fun `ModifierPhonemeMatcher filters by multiple PhonemeCharacteristics`() {
        val matcher = CharacteristicPhonemeMatcher(ArticulationPlace.Alveolar, ArticulationManner.Stop)

        assertEquals(
            listOf("t", "tt", "d", "dd").map { testPhonemeContainer.getPhoneme(it) },
            testPhonemeContainer.phonemes.filter { matcher.match(it) }
        )
    }

    @Test
    fun `AbsentModifierPhonemeMatcher filters by multiple PhonemeCharacteristics`() {
        val matcher = AbsentCharacteristicPhonemeMatcher(ArticulationPlace.Alveolar, Voiced)

        assertEquals(
            listOf("p", "pp", "c", "cc").map { testPhonemeContainer.getPhoneme(it) },
            testPhonemeContainer.phonemes.filter { matcher.match(it) }
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
                CharacteristicPhonemeMatcher(Labialized),
                CharacteristicPhonemeMatcher(Nasalized, PhonemeModifier.Long),
                CharacteristicPhonemeMatcher(Nasalized),
                CharacteristicPhonemeMatcher(Palatilized),
                CharacteristicPhonemeMatcher(ArticulationManner.Stop),
                CharacteristicPhonemeMatcher(ArticulationPlace.Alveolar),
                CharacteristicPhonemeMatcher(ArticulationPlace.Alveolar, ArticulationManner.Stop),
                CharacteristicPhonemeMatcher(ArticulationManner.Stop, PhonemeModifier.Long),
                AbsentCharacteristicPhonemeMatcher(Labialized),
                AbsentCharacteristicPhonemeMatcher(Nasalized, PhonemeModifier.Long),
                AbsentCharacteristicPhonemeMatcher(Nasalized),
                AbsentCharacteristicPhonemeMatcher(Palatilized),
                AbsentCharacteristicPhonemeMatcher(ArticulationManner.Stop),
                AbsentCharacteristicPhonemeMatcher(ArticulationPlace.Alveolar),
                AbsentCharacteristicPhonemeMatcher(ArticulationPlace.Alveolar, ArticulationManner.Stop),
                AbsentCharacteristicPhonemeMatcher(ArticulationManner.Stop, PhonemeModifier.Long),
                ProsodyMatcher(Prosody.Stress),
                AbsentProsodyMatcher(Prosody.Stress),
                MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long)),
                MulMatcher(TypePhonemeMatcher(Vowel), AbsentCharacteristicPhonemeMatcher(Nasalized, Labialized)),
                MulMatcher(AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long), AbsentCharacteristicPhonemeMatcher(Nasalized, Labialized)),
            )

            return composeUniquePairs(matchers, matchers)
                .map { (f, s) -> Arguments.of(f, s) }
                .stream()
        }
    }
}