package io.tashtabash.lang.language.morphem.change.substitution

import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeModifier.*
import io.tashtabash.lang.language.util.getTestPhoneme
import io.tashtabash.lang.language.util.testPhonemeContainer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


internal class PhonemeSubstitutionTest {
    @ParameterizedTest(name = "{0} combined with {1} = {2}")
    @MethodSource("phonemeSubstitutionProvider")
    fun `times method is CORRECT (I'm too lazy to write a description for every one of them)`(
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
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(DeletingPhonemeSubstitution, ExactPhonemeSubstitution(getTestPhoneme("t")))
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
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(DeletingPhonemeSubstitution),
                listOf(DeletingPhonemeSubstitution)
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("d"))),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("d")))
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("d")))
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(getTestPhoneme("d"))),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t")))
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
            Arguments.of(
                listOf<PhonemeSubstitution>(),
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(EpenthesisSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf<PhonemeSubstitution>(),
                listOf(EpenthesisSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(DeletingPhonemeSubstitution),
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(DeletingPhonemeSubstitution, EpenthesisSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(DeletingPhonemeSubstitution),
                listOf(DeletingPhonemeSubstitution)
            ),
            Arguments.of(
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(EpenthesisSubstitution(getTestPhoneme("t")), ExactPhonemeSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(PassingPhonemeSubstitution),
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(EpenthesisSubstitution(getTestPhoneme("t")), PassingPhonemeSubstitution),
            ),
            Arguments.of(
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(PassingPhonemeSubstitution),
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
            ),
            Arguments.of(
                listOf(PassingPhonemeSubstitution, ExactPhonemeSubstitution(getTestPhoneme("t"))),
                listOf(ExactPhonemeSubstitution(getTestPhoneme("d")), EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(
                    ExactPhonemeSubstitution(getTestPhoneme("d")),
                    EpenthesisSubstitution(getTestPhoneme("t")),
                    ExactPhonemeSubstitution(getTestPhoneme("t"))
                ),
            ),
            Arguments.of(
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(EpenthesisSubstitution(getTestPhoneme("d")))
            ),
            Arguments.of(
                listOf(EpenthesisSubstitution(getTestPhoneme("d"))),
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(EpenthesisSubstitution(getTestPhoneme("t")))
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer)),
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(EpenthesisSubstitution(getTestPhoneme("t")), ModifierPhonemeSubstitution(setOf(Voiced), setOf(), testPhonemeContainer))
            ),
            Arguments.of(
                listOf(ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer)),
                listOf(EpenthesisSubstitution(getTestPhoneme("t"))),
                listOf(EpenthesisSubstitution(getTestPhoneme("t")), ModifierPhonemeSubstitution(setOf(), setOf(Voiced), testPhonemeContainer))
            ),
        )
    }
}
