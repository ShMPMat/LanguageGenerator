package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.diachronicity.tendency.Devoicing
import io.tashtabash.lang.language.util.createNoun
import io.tashtabash.lang.language.util.makeDefLang
import io.tashtabash.lang.language.util.testPhonemeContainer
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.random.Random
import kotlin.test.assertContains


internal class TendencyBasedPhonologicalRuleApplicatorTest {
    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    fun `applyPhonologicalRule() applies random rules if no tendency exists`() {
        (1..100).map {
            RandomSingleton.safeRandom = Random(Random.nextInt())
            val applicator = TendencyBasedPhonologicalRuleApplicator(testPhonemeContainer, tendencyDevelopmentChance = 0.0)
            val language = makeDefLang(
                listOf(createNoun("a"))
            )
            val phonologicalRulesContainer = PhonologicalRulesContainer(
                listOf(
                    createPhonologicalRule("o -> a / _", testPhonemeContainer)
                )
            )

            applicator.applyPhonologicalRule(language, phonologicalRulesContainer)

            assertContains(
                applicator.messages,
                "No tendencies, applying a random rule"
            )
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    fun `applyPhonologicalRule() immediately applies a tendency if the development chance is 1`() {
        (1..100).map {
            RandomSingleton.safeRandom = Random(Random.nextInt())
            val applicator = TendencyBasedPhonologicalRuleApplicator(
                testPhonemeContainer,
                tendencyDevelopmentChance = 1.0,
                possibleTendencies = mutableListOf(Devoicing())
            )
            val language = makeDefLang(
                listOf(createNoun("a"))
            )
            val phonologicalRulesContainer = PhonologicalRulesContainer(
                listOf(
                    createPhonologicalRule("o -> a / _", testPhonemeContainer)
                )
            )

            applicator.applyPhonologicalRule(language, phonologicalRulesContainer)

            assertTrue {
                applicator.messages.any { it.contains("A new tendency is developed") }
            }
        }
    }
}
