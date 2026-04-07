package io.tashtabash.lang.generator

import io.tashtabash.lang.language.syntax.SyntaxParadigm
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import io.tashtabash.lang.language.syntax.clause.construction.PredicatePossessionConstruction.*
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.withProb
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


class WordOrderGeneratorTest {
    @Test
    fun `Can generate languages with additional rules for the SOV word order`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        val syntaxParadigm = SyntaxParadigm(
            CopulaPresence(listOf(CopulaConstruction.None.withProb(1.0))),
            QuestionMarkerPresence(null),
            PredicatePossessionPresence(listOf(HaveVerb.withProb(1.0)))
        )

        assertTrue {
            (1..100).map {
                WordOrderGenerator().generateWordOrder(syntaxParadigm)
            }.any { it.sovOrder.map.isNotEmpty() }
        }
    }

    @Test
    fun `Can generate languages with all copula types`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        val syntaxParadigm = SyntaxParadigm(
            CopulaPresence(listOf(
                CopulaConstruction.None.withProb(1.0),
                CopulaConstruction.Verb.withProb(1.0),
                CopulaConstruction.Particle.withProb(1.0)
            )),
            QuestionMarkerPresence(null),
            PredicatePossessionPresence(listOf(HaveVerb.withProb(1.0)))
        )

        assertEquals(
            listOf(CopulaConstruction.None, CopulaConstruction.Verb, CopulaConstruction.Particle)
                .sortedBy { it.javaClass.simpleName },
            (1..100).map {
                WordOrderGenerator().generateWordOrder(syntaxParadigm)
            }.flatMap { it.copulaOrder.keys }
                .distinct()
                .sortedBy { it.javaClass.simpleName }
        )
    }
}
