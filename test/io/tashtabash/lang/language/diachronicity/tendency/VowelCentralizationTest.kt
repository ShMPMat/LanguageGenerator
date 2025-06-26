package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.language.diachronicity.createPhonologicalRules
import io.tashtabash.lang.language.util.makeDefLang
import io.tashtabash.lang.language.util.testPhonemeContainer
import io.tashtabash.lang.language.util.testPhonemeContainerExtended
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertContains


class VowelCentralizationTest {
    @Test
    fun `getOptions() returns correct options`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        val languagePhonemes = ImmutablePhonemeContainer(
            testPhonemeContainer.getPhonemes("t", "d", "u")
        )
        val language = makeDefLang(listOf())
            .copy(phonemeContainer = languagePhonemes)

        for (i in 1..100) {
            assertContains(
                testPhonemeContainerExtended.createPhonologicalRules {
                    listOf(
                        createRule("u -> ʊ / _ "),
                        createRule("u -> ʉ / _ ")
                    )
                },
                VowelCentralization().getRule(language, testPhonemeContainerExtended)
            )
        }
    }
}
