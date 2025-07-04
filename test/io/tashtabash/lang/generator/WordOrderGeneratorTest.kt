package io.tashtabash.lang.generator

import io.tashtabash.lang.language.syntax.SyntaxParadigm
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


class WordOrderGeneratorTest {
    @Test
    fun `Can generate languages with additional rules for the SOV word order`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        val syntaxParadigm = SyntaxParadigm(
            CopulaPresence(listOf(CopulaType.None.toSso(1.0))),
            QuestionMarkerPresence(null),
            PredicatePossessionPresence(listOf(PredicatePossessionType.HaveVerb.toSso(1.0)))
        )

        assertTrue {
            (1..100).map {
                WordOrderGenerator().generateWordOrder(syntaxParadigm)
            }.any { it.sovOrder.map.isNotEmpty() }
        }
    }
}
