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
    fun `times method is commutative for its first member`(first: PhonemeMatcher, second: PhonemeMatcher) {
        assertEquals(
            (first * second)?.first,
            (second * first)?.first
        )
    }

    @Test
    fun `isNarrowed for CharacteristicPhonemeMatcher depends on the operand order`() {
        assertEquals(
            CharacteristicPhonemeMatcher(Nasalized, PhonemeModifier.Long) to false,
            CharacteristicPhonemeMatcher(Nasalized, PhonemeModifier.Long) * CharacteristicPhonemeMatcher(Nasalized)
        )
        assertEquals(
            CharacteristicPhonemeMatcher(Nasalized, PhonemeModifier.Long) to true,
            CharacteristicPhonemeMatcher(Nasalized) * CharacteristicPhonemeMatcher(Nasalized, PhonemeModifier.Long)
        )
    }

    @Test
    fun `ExactPhonemeMatcher is almost always not isNarrowed, but not vise versa`() {
        assertEquals(
            ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d")) to false,
            ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d")) * CharacteristicPhonemeMatcher(Voiced)
        )
        assertEquals(
            ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d")) to true,
            CharacteristicPhonemeMatcher(Voiced) * ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d"))
        )

        assertEquals(
            ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d")) to false,
            ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d")) * TypePhonemeMatcher(Consonant)
        )
        assertEquals(
            ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d")) to true,
            TypePhonemeMatcher(Consonant) * ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("d"))
        )
    }

    @Test
    fun `MulMatcher times MulMatcher filters out duplicate matchers`() {
        val matcher = MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long))

        assertEquals(matcher to false, matcher * matcher)
    }

    @Test
    fun `MulMatcher times MulMatcher merges ModifierPhonemeMatcher if possible`() {
        assertEquals(
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long, Voiced)) to true,
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(PhonemeModifier.Long))
                    * AbsentCharacteristicPhonemeMatcher(Voiced)
        )
    }

    @Test
    fun `MulMatcher times ExactPhonemeMatcher results in ExactPhonemeMatcher if the merge is possible`() {
        val matcher = ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("t"))
        assertEquals(
            matcher to true,
            MulMatcher(TypePhonemeMatcher(Consonant), AbsentCharacteristicPhonemeMatcher(Voiced))
                    * ExactPhonemeMatcher(testPhonemeContainer.getPhoneme("t"))
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