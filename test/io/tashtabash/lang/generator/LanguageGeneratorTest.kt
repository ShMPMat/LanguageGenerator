package io.tashtabash.lang.generator

import io.tashtabash.lang.containers.WordBase
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Test
import kotlin.random.Random


class LanguageGeneratorTest {
    @Test
    fun `LanguageGenerator generates 100 languages without an error`() {
        repeat(100) {
            val seed = Random.nextInt()

            try {
                RandomSingleton.safeRandom = Random(seed)
                val generator = LanguageGenerator("SupplementFiles")
                val wordAmount = WordBase("SupplementFiles").baseWords.size
                generator.generateLanguage(wordAmount)
            } catch (e: Exception) {
                error("Error generating language with seed $seed: $e")
            }
        }
    }
}
