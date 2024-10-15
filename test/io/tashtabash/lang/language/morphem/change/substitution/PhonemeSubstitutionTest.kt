package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeModifier.*
import io.tashtabash.lang.language.util.testPhonemeContainer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


internal class PhonemeSubstitutionTest {
    @ParameterizedTest(name = "{0} combined with {1} = {2}")
    @MethodSource("phonemeSubstitutionProvider")
    fun `times method is commutative`(
        old: List<PhonemeSubstitution?>,
        new: List<PhonemeSubstitution?>,
        result: List<PhonemeSubstitution>
    ) {
        assertEquals(
            result,
            unitePhonemeSubstitutions(old, new)
        )
    }

    companion object {
        @JvmStatic
        fun phonemeSubstitutionProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(
                listOf(DeletingPhonemeSubstitution),
                listOf(DeletingPhonemeSubstitution),
                listOf(DeletingPhonemeSubstitution, DeletingPhonemeSubstitution)
            ),
            Arguments.of(
                listOf<PhonemeSubstitution>(),
                listOf(DeletingPhonemeSubstitution),
                listOf(DeletingPhonemeSubstitution)
            ),
            Arguments.of(
                listOf(PassingPhonemeSubstitution),
                listOf<PhonemeSubstitution>(),
                listOf(PassingPhonemeSubstitution)
            ),
            Arguments.of(
                listOf(DeletingPhonemeSubstitution),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t"))),
                listOf(DeletingPhonemeSubstitution, ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t")))
            ),
            Arguments.of(
                listOf(DeletingPhonemeSubstitution),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced, Labialized), setOf(), testPhonemeContainer)),
                listOf(DeletingPhonemeSubstitution, ModifierPhonemeSubstitution(setOf(Voiced, Labialized), setOf(), testPhonemeContainer))
            ),
            Arguments.of(
                listOf(DeletingPhonemeSubstitution),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced, Labialized), testPhonemeContainer)),
                listOf(DeletingPhonemeSubstitution, ModifierPhonemeSubstitution(setOf(), setOf(Voiced, Labialized), testPhonemeContainer))
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t"))),
                listOf(DeletingPhonemeSubstitution),
                listOf(DeletingPhonemeSubstitution)
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("d"))),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("d")))
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t"))),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("d")))
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("d"))),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t")))
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t")))
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme("t")))
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced, Labialized), setOf(), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced, Labialized), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(Labialized), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(Voiced), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced, PhonemeModifier.Long), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(Voiced, PhonemeModifier.Long), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Voiced, PhonemeModifier.Long), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced, PhonemeModifier.Long), setOf(Labialized), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Voiced, PhonemeModifier.Long), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(PhonemeModifier.Long), setOf(Voiced), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced, PhonemeModifier.Long), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(PhonemeModifier.Long), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(PassingPhonemeSubstitution),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(Labialized), setOf(), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
            ),
            Arguments.of(
                listOf(PassingPhonemeSubstitution),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Labialized), testPhonemeContainer)),
            ),
        )
    }
}
