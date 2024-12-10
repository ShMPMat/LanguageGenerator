package io.tashtabash.lang.language.morpheme

import io.tashtabash.lang.language.util.createNoun
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.tashtabash.lang.language.util.createTemplateChange
import io.tashtabash.lang.language.util.withMorphemes


internal class WordTemplateSingleChangeTest {
    @Test
    fun `TemplateChange correctly agglutinates a prefix`() {
        val changeTemplate = createTemplateChange("ba-")
        val firstWord = createNoun("ba")
        val secondWord = createNoun("ca")

        assertEquals(
            createNoun("baba").withMorphemes(1, 2, 2),
            changeTemplate.change(firstWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("baca").withMorphemes(1, 2, 2),
            changeTemplate.change(secondWord, listOf(), listOf())
        )
    }

    @Test
    fun `TemplateChange correctly adds a prefix on a phoneme type condition`() {
        val changeTemplate = createTemplateChange("C- -> ba_")
        val matchingWord = createNoun("ba")
        val nonMatchingWord = createNoun("aca")

        assertEquals(
            createNoun("baba").withMorphemes(1, 2, 2),
            changeTemplate.change(matchingWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("aca"),
            changeTemplate.change(nonMatchingWord, listOf(), listOf())
        )
    }

    @Test
    fun `TemplateChange correctly adds a prefix on a phoneme condition`() {
        val changeTemplate = createTemplateChange("b- -> ba_")
        val matchingWord = createNoun("ba")
        val nonMatchingWord = createNoun("ca")

        assertEquals(
            createNoun("baba").withMorphemes(1, 2, 2),
            changeTemplate.change(matchingWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("ca"),
            changeTemplate.change(nonMatchingWord, listOf(), listOf())
        )
    }
    @Test
    fun `TemplateChange correctly agglutinates a postfix`() {
        val changeTemplate = createTemplateChange("-ba")
        val firstWord = createNoun("ba")
        val secondWord = createNoun("ca")

        assertEquals(
            createNoun("baba").withMorphemes(0, 2, 2),
            changeTemplate.change(firstWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("caba").withMorphemes(0, 2, 2),
            changeTemplate.change(secondWord, listOf(), listOf())
        )
    }

    @Test
    fun `TemplateChange correctly adds a postfix on a phoneme type condition`() {
        val changeTemplate = createTemplateChange("-C -> _ba")
        val matchingWord = createNoun("bab")
        val nonMatchingWord = createNoun("aca")

        assertEquals(
            createNoun("babba").withMorphemes(0, 3, 2),
            changeTemplate.change(matchingWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("aca"),
            changeTemplate.change(nonMatchingWord, listOf(), listOf())
        )
    }

    @Test
    fun `TemplateChange correctly adds a postfix on a phoneme condition`() {
        val changeTemplate = createTemplateChange("-b -> _ba")
        val matchingWord = createNoun("bab")
        val nonMatchingWord = createNoun("cac")

        assertEquals(
            createNoun("babba").withMorphemes(0, 3, 2),
            changeTemplate.change(matchingWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("cac"),
            changeTemplate.change(nonMatchingWord, listOf(), listOf())
        )
    }

    @Test
    fun `TemplateChange correctly deletes a boundary phoneme`() {
        val changeTemplate = createTemplateChange("_- -> ba-")
        val firstWord = createNoun("ba")
        val secondWord = createNoun("ca")

        assertEquals(
            createNoun("baa").withMorphemes(1, 2, 1),
            changeTemplate.change(firstWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("baa").withMorphemes(1, 2, 1),
            changeTemplate.change(secondWord, listOf(), listOf())
        )
    }

    @Test
    fun `TemplateChange suffix matches an opposite border`() {
        val changeTemplate = createTemplateChange("-\$CaC -> __-_ob")
        val firstWord = createNoun("cab")
        val secondWord = createNoun("ca")

        assertEquals(
            createNoun("cbob").withMorphemes(0, 2, 2),
            changeTemplate.change(firstWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("ca"),
            changeTemplate.change(secondWord, listOf(), listOf())
        )
    }

    @Test
    fun `TemplateChange prefix matches an opposite border`() {
        val changeTemplate = createTemplateChange("CaC\$- -> bo_-__")
        val firstWord = createNoun("cab")
        val secondWord = createNoun("ca")

        assertEquals(
            createNoun("bocb").withMorphemes(1, 2, 2),
            changeTemplate.change(firstWord, listOf(), listOf())
        )
        assertEquals(
            createNoun("ca"),
            changeTemplate.change(secondWord, listOf(), listOf())
        )
    }
}
