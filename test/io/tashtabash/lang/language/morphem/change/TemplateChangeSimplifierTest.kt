package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.util.createAffix
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TemplateChangeSimplifierTest {
    @Test
    fun `createSimplifiedTemplateChange doesn't wrap a single change into TemplateSequenceChange`() {
        val templateSingleChange = createAffix("-t -> _b").templateChange

        val result = createSimplifiedTemplateChange(listOf(templateSingleChange))

        assertEquals(templateSingleChange, result)
    }

    @Test
    fun `createSimplifiedTemplateChange collapses similar rules starting from different phoneme types`() {
        val result = createSimplifiedTemplateChange(listOf(
            createAffix("-t -> _b").templateChange,
            createAffix("-C -> ab").templateChange,
            createAffix("-V -> ab").templateChange,
            createAffix("-ab -> _t").templateChange,
        ))

        assertEquals(
            TemplateSequenceChange(listOf(
                createAffix("-t -> _b").templateChange,
                createAffix("-_ -> ab").templateChange,
                createAffix("-ab -> _t").templateChange,
            )),
            result
        )
    }

    @Test
    fun `createSimplifiedTemplateChange eliminates the combo of passing substitution + passing matcher`() {
        val result = createSimplifiedTemplateChange(listOf(
            createAffix("-t -> _b").templateChange,
            createAffix("-C -> _b").templateChange,
            createAffix("-V -> _b").templateChange,
            createAffix("-ab -> _t").templateChange,
        ))

        assertEquals(
            TemplateSequenceChange(listOf(
                createAffix("-t -> _b").templateChange,
                createAffix("- -> b").templateChange,
                createAffix("-ab -> _t").templateChange,
            )),
            result
        )
    }
}