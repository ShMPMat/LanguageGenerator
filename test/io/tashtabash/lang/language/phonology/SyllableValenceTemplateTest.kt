package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.phonology.PhonemeType.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SyllableValenceTemplateTest {
    @Test
    fun `SyllableValenceTemplateCorrectly merges similar syllable templates`() {
        val firstTemplate = SyllableValenceTemplate(
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.9),
            ValencyPlace(Vowel, 1.0),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5)
        )
        val secondTemplate = SyllableValenceTemplate(
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.9),
            ValencyPlace(Vowel, 1.0),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5)
        )

        assertEquals(
            firstTemplate.merge(secondTemplate),
            SyllableValenceTemplate(
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.9),
                ValencyPlace(Vowel, 1.0),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5)
            )
        )
    }

    @Test
    fun `SyllableValenceTemplateCorrectly merges syllable templates where the second one has a longer initial`() {
        val firstTemplate = SyllableValenceTemplate(
            ValencyPlace(Consonant, 0.9),
            ValencyPlace(Vowel, 1.0),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5)
        )
        val secondTemplate = SyllableValenceTemplate(
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.9),
            ValencyPlace(Vowel, 1.0),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5)
        )

        assertEquals(
            firstTemplate.merge(secondTemplate),
            SyllableValenceTemplate(
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.9),
                ValencyPlace(Vowel, 1.0),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5)
            )
        )
    }

    @Test
    fun `SyllableValenceTemplateCorrectly merges syllable templates where the first one has a longer initial`() {
        val firstTemplate = SyllableValenceTemplate(
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.9),
            ValencyPlace(Vowel, 1.0),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5)
        )
        val secondTemplate = SyllableValenceTemplate(
            ValencyPlace(Vowel, 1.0),
            ValencyPlace(Consonant, 0.5),
            ValencyPlace(Consonant, 0.5)
        )

        assertEquals(
            firstTemplate.merge(secondTemplate),
            SyllableValenceTemplate(
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.9),
                ValencyPlace(Vowel, 1.0),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5),
                ValencyPlace(Consonant, 0.5)
            )
        )
    }
}