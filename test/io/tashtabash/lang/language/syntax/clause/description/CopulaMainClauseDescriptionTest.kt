package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.lexis.SpeechPart.Noun
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.Past
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.Indicative
import io.tashtabash.lang.language.syntax.context.PrioritizedValue
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.language.util.*
import io.tashtabash.lang.utils.MapWithDefault
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class CopulaDescriptionTest {
    @Test
    fun `No copula set-up works`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up WordChangeParadigm
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()))
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createNoun("o") withMeaning "cat"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                copulaCaseSolver = mapOf(
                    CopulaType.None to SyntaxRelation.Agent to Noun.toDefault() to listOf(),
                    CopulaType.None to SyntaxRelation.SubjectCompliment to Noun.toDefault() to listOf()
                )
            ),
            copulaOrder = mapOf(
                CopulaType.None to MapWithDefault(
                    RelationArranger(
                        SubstitutingOrder(
                            NestedOrder(defOrder, NominalGroupOrder.DNP, SyntaxRelation.Agent),
                            mapOf(SyntaxRelation.Patient to SyntaxRelation.SubjectCompliment)
                        )
                    )
                )
            )
        )
        val sentenceDescription = CopulaMainClauseDescription(
            CopulaDescription(
                NominalDescription("dog", ContextValue.ActorComplimentValue(1)),
                NominalDescription("cat", ContextValue.ActorComplimentValue(1))
            )
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Indicative, Explicit)
        )

        assertEquals(
            listOf(
                createNoun("i") withMeaning "dog",
                createNoun("o") withMeaning "cat"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}
