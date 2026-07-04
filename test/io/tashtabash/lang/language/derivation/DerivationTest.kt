package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.containers.SemanticsCoreTemplate
import io.tashtabash.lang.language.derivation.DerivationClass.AbstractNounFromNoun
import io.tashtabash.lang.language.derivation.DerivationType.NNAbstract
import io.tashtabash.lang.language.lexis.DerivationCluster
import io.tashtabash.lang.language.lexis.DerivationLink
import io.tashtabash.lang.language.lexis.SimpleMutableLexis
import io.tashtabash.lang.language.lexis.SimpleWordPointer
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.morphem.PassingWordChange
import io.tashtabash.lang.language.util.createAffix
import io.tashtabash.lang.language.util.createIntransVerb
import io.tashtabash.lang.language.util.createNoun
import io.tashtabash.lang.language.util.defCategoryChanger
import io.tashtabash.lang.language.util.defSpeechPart
import io.tashtabash.lang.language.util.withMorphemes
import io.tashtabash.lang.language.util.withTags
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class DerivationTest {
    @Test
    fun `Returns word if applied to an appropriate speech part`() {
        val derivation = Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, Double.MAX_VALUE, defCategoryChanger)
        val word = createNoun("ba").let { w ->
            w.copy(
                semanticsCore = w.semanticsCore.copy(
                    derivationCluster = DerivationCluster(mapOf(NNAbstract to listOf(DerivationLink("phony", 1.0))))
                )
            )
        }
        val lexis = SimpleMutableLexis(word)

        assertEquals(
            derivation.derive(word, lexis, Random(Random.nextInt())) {
                SemanticsCoreTemplate(
                    "phony",
                    SpeechPart.Noun
                )
            },
            createNoun("baab")
                .withMorphemes(
                    MorphemeData(2, listOf(), true),
                    MorphemeData(2, derivationValues = listOf(AbstractNounFromNoun)),
                ).withTags(AbstractNounFromNoun.toString())
                .let { w ->
                    w.copy(
                        semanticsCore = w.semanticsCore.copy(
                            changeHistory = DerivationHistory(derivation, SimpleWordPointer(word))
                        )
                    )
                }
        )
    }

    @Test
    fun `Returns null if applied to an inappropriate speech part`() {
        val derivation = Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, Double.MAX_VALUE, defCategoryChanger)
        val word = createIntransVerb("ba")
        val lexis = SimpleMutableLexis(word)

        assertEquals(
            derivation.derive(word, lexis, Random(Random.nextInt())) {
                SemanticsCoreTemplate(
                    "phony",
                    SpeechPart.Noun
                )
            },
            null
        )
    }

    @Test
    fun `Adds the DerivationClass to the root for PassingWordChange`() {
        val derivation = Derivation(PassingWordChange, AbstractNounFromNoun, defSpeechPart, Double.MAX_VALUE, defCategoryChanger)
        val word = createNoun("ba").let { w ->
            w.copy(
                semanticsCore = w.semanticsCore.copy(
                    derivationCluster = DerivationCluster(mapOf(NNAbstract to listOf(DerivationLink("phony", 1.0))))
                )
            )
        }
        val lexis = SimpleMutableLexis(word)

        assertEquals(
            derivation.derive(word, lexis, Random(Random.nextInt())) {
                SemanticsCoreTemplate(
                    "phony",
                    SpeechPart.Noun
                )
            },
            createNoun("ba")
                .withMorphemes(
                    MorphemeData(2, listOf(), true, listOf(AbstractNounFromNoun)),
                ).withTags(AbstractNounFromNoun.toString())
                .let { w ->
                    w.copy(
                        semanticsCore = w.semanticsCore.copy(
                            changeHistory = DerivationHistory(derivation, SimpleWordPointer(word))
                        )
                    )
                }
        )
    }
}