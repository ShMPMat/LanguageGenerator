package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.random.Random


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
}