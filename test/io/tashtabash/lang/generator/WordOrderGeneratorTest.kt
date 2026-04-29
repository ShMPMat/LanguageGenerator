package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.SpeechPart.Verb
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxParadigm
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import io.tashtabash.lang.language.syntax.clause.construction.PotentialConstruction
import io.tashtabash.lang.language.syntax.clause.construction.PredicatePossessionConstruction.*
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.lang.language.syntax.transformer.ChangeOrderTransformer
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
            PredicatePossessionPresence(listOf(HaveVerb.withProb(1.0))),
            PotentialConstruction.Adverb
        )

        assertTrue {
            (1..100).map {
                TransformerGenerator(
                    WordChangeParadigm(
                        listOf(),
                        mapOf(
                            SpeechPart.PersonalPronoun.toDefault() to SpeechPartChangeParadigm(SpeechPart.PersonalPronoun.toDefault())
                        ),
                        listOf()
                    ),
                    SyntaxLogic(
                        verbCasesSolver = mapOf(
                            Verb.toDefault() to SyntaxRelation.Agent to listOf(),
                            Verb.toDefault() to SyntaxRelation.Patient to listOf()
                        )
                    ),
                    WordOrderGenerator().generateWordOrder(syntaxParadigm),
                    syntaxParadigm
                ).generateTransformers()
            }.any { result -> result.any { it.second is ChangeOrderTransformer } }
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
            PredicatePossessionPresence(listOf(HaveVerb.withProb(1.0))),
            PotentialConstruction.Adverb
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
