package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.random.Random
import kotlin.test.assertContains


internal class RandomPhonologicalRuleApplicatorTest {
    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    fun `chooseRule() narrows rules`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        val language = makeDefLang(
            listOf(createNoun("a")),
            listOf(),
            makeDefNounChangeParadigm(
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                PassingCategoryApplicator
            )
        )
        val phonologicalRulesContainer = PhonologicalRulesContainer(listOf(
            createPhonologicalRule("C -> a / _", testPhonemeContainer)
        ))
        val randomPhonologicalRuleApplicator = RandomPhonologicalRuleApplicator(1.0)

        assertNotEquals(
            randomPhonologicalRuleApplicator.chooseRule(language, phonologicalRulesContainer),
            phonologicalRulesContainer.phonologicalRules[0]
        )
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    fun `chooseRule() doesn't narrow rules`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        val language = makeDefLang(
            listOf(createNoun("a")),
            listOf(),
            makeDefNounChangeParadigm(
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                PassingCategoryApplicator
            )
        )
        val phonologicalRulesContainer = PhonologicalRulesContainer(listOf(
            createPhonologicalRule("C -> a / _", testPhonemeContainer)
        ))
        val randomPhonologicalRuleApplicator = RandomPhonologicalRuleApplicator(0.0)

        assertEquals(
            randomPhonologicalRuleApplicator.chooseRule(language, phonologicalRulesContainer),
            phonologicalRulesContainer.phonologicalRules[0]
        )
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    fun `chooseRule() narrows ExactPhonemeMatchers with Stress`() {
        val randomPhonologicalRuleApplicator = RandomPhonologicalRuleApplicator(0.5)
        val rules = (1..100).map {
            RandomSingleton.safeRandom = Random(Random.nextInt())
            val language = makeDefLang(
                listOf(createNoun("a").withProsodyOn(0, Prosody.Stress)),
                listOf(),
                makeDefNounChangeParadigm(
                    PassingCategoryApplicator,
                    PassingCategoryApplicator,
                    PassingCategoryApplicator,
                    PassingCategoryApplicator
                )
            ).copy(stressType = StressType.NotFixed)
            val phonologicalRulesContainer = PhonologicalRulesContainer(
                listOf(
                    createPhonologicalRule("o -> a / _", testPhonemeContainer)
                )
            )

            randomPhonologicalRuleApplicator.chooseRule(language, phonologicalRulesContainer)
        }

        assertContains(
            rules,
            createPhonologicalRule("(o{+Stress}) -> a / _", testPhonemeContainer)
        )
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    fun `chooseRule() doesn't narrow ExactPhonemeMatchers with Stress if Language has no stress`() {
        val randomPhonologicalRuleApplicator = RandomPhonologicalRuleApplicator(0.5)
        val rules = (1..100).map {
            RandomSingleton.safeRandom = Random(Random.nextInt())
            val language = makeDefLang(
                listOf(createNoun("a").withProsodyOn(0, Prosody.Stress)),
                listOf(),
                makeDefNounChangeParadigm(
                    PassingCategoryApplicator,
                    PassingCategoryApplicator,
                    PassingCategoryApplicator,
                    PassingCategoryApplicator
                )
            ).copy(stressType = StressType.None)
            val phonologicalRulesContainer = PhonologicalRulesContainer(
                listOf(
                    createPhonologicalRule("o -> a / _", testPhonemeContainer)
                )
            )

            randomPhonologicalRuleApplicator.chooseRule(language, phonologicalRulesContainer)
        }

        assertFalse {
            rules.contains(createPhonologicalRule("(o{+Stress}) -> a / _", testPhonemeContainer))
        }
    }
}