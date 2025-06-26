package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.language.util.makeDefLang
import io.tashtabash.lang.language.util.testPhonemeContainerExtended
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


class PhonologicalChangeTendencyTest {
    @Test
    fun `All default Tendencies return the instances of the same tendency in getNewInstance()`() {
        for (tendency in createDefaultPhonologicalChangeTendencies())
            assertEquals(
                tendency.name,
                tendency.getNewInstance().name
            )
    }

    @Test
    fun `All default Tendencies do not throw in getRule()`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        val language = makeDefLang(listOf())

        for (i in 1..100)
            for (tendency in createDefaultPhonologicalChangeTendencies())
                assertDoesNotThrow {
                    tendency.getRule(language, testPhonemeContainerExtended)
                }
    }
}
