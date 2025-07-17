package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.util.createNoun
import io.tashtabash.lang.language.util.createTemplateChange
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis


internal class TemplateSingleChangeTest {
    // I did a cursory search to find a good framework for that, but there wasn't one.
    @Test
    fun `change doesn't take too long`() {
        val runs = 5000
        var totalTime = 0L
        val maxTime = 0.05 // Seems like it's ~3 times faster if the full suite is running (07.01.2025)

        val templateChange = createTemplateChange("-aC(V[+Labialized]) -> o-_data")
        val word = createNoun("tadu")

        for (i in 1..runs)
            totalTime += measureTimeMillis {
                templateChange.change(word, listOf(), listOf())
            }
        val avgTime = totalTime.toDouble() / runs

        // Before the PhonologicalRule-based TemplateChanges ~0.015ms, after - ~0.023
        println("Average change application time over $runs runs: $avgTime ms (total time $totalTime ms)")
        assertTrue(avgTime <= maxTime, "Avg time was $avgTime > $maxTime ms")
    }
}
