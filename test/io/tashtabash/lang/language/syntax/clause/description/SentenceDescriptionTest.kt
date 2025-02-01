package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.NumeralSystemBase
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.derivation.DerivationParadigm
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.phonology.RestrictionsParadigm
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue
import io.tashtabash.lang.language.syntax.context.PrioritizedValue
import io.tashtabash.lang.language.syntax.context.Priority
import io.tashtabash.lang.language.syntax.features.CopulaPresence
import io.tashtabash.lang.language.syntax.features.PredicatePossessionPresence
import io.tashtabash.lang.language.syntax.features.QuestionMarkerPresence
import io.tashtabash.lang.language.syntax.features.toSso
import io.tashtabash.lang.language.syntax.numeral.NumeralParadigm
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.toSampleSpaceObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class SentenceDescriptionTest {
    @Test
    fun `Verbs with Tense pick up it from context`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up tense
        val tenseCategory = Tense(
            listOf(TenseValue.Present, TenseValue.Past),
            setOf(
                PSpeechPart(SpeechPart.Verb, CategorySource.Self)
            ),
            setOf(SpeechPart.Verb)
        )
        val tenseSourcedCategory = SourcedCategory(
            tenseCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val tenseExponenceCluster = ExponenceCluster(listOf(tenseSourcedCategory))
        // Set up WordChangeParadigm
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive(),
            listOf(tenseExponenceCluster),
            mapOf(tenseExponenceCluster to tenseExponenceCluster.possibleValues.zip(tenseApplicators).toMap()),
            ProsodyChangeParadigm(StressType.None)
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                SpeechPart.Noun.toDefault() to SpeechPartChangeParadigm(SpeechPart.Noun.toDefault(), listOf(), mapOf(), ProsodyChangeParadigm(StressType.None)),
            )
        )
        val lexis = Lexis(
            listOf(
                createNoun("i").withMeaning("dog"),
                createIntransVerb("o").withMeaning("sleep")
            ),
            mapOf(),
            mapOf()
        )
        val language = Language(
            lexis,
            testPhonemeContainer,
            StressType.Initial,
            RestrictionsParadigm(mutableMapOf()),
            DerivationParadigm(listOf(), listOf()),
            ChangeParadigm(
                WordOrder(
                    mapOf(
                        VerbSentenceType.MainVerbClause to
                                SovOrder(listOf(listOf(SyntaxRelation.Agent, SyntaxRelation.Verb).toSampleSpaceObject(1.0)), "Name")),
                    mapOf(),
                    NominalGroupOrder.DNP
                ),
                wordChangeParadigm,
                SyntaxParadigm(
                    CopulaPresence(listOf(io.tashtabash.lang.language.syntax.features.CopulaType.None.toSso(1.0))),
                    QuestionMarkerPresence(null),
                    PredicatePossessionPresence(listOf(io.tashtabash.lang.language.syntax.features.PredicatePossessionType.HaveVerb.toSso(1.0)))
                ),
                NumeralParadigm(NumeralSystemBase.Restricted3, listOf()),
                SyntaxLogic(
                    mapOf(
                        SpeechPart.Verb.toIntransitive() to ContextValue.TimeContext.Past to listOf(SourcedCategoryValue(TenseValue.Past, CategorySource.Self, tenseSourcedCategory))
                    ),
                    mapOf(
                        SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Argument to listOf()
                    ),
                    mapOf(),
                    mapOf(),
                    null,
                    mapOf(),
                    mapOf(),
                    listOf(),
                    null
                )
            )
        )
        val sentenceDescription = IntransitiveVerbMainClauseDescription(
            SimpleIntransitiveVerbDescription(
                "sleep",
                NominalDescription(
                    "dog",
                    listOf(),
                    ContextValue.ActorComplimentValue(ContextValue.Amount.AmountValue(3), null)
                )
            )
        )
        val context = Context(
            PrioritizedValue(ContextValue.TimeContext.Past, Priority.Implicit),
            PrioritizedValue(ContextValue.TypeContext.Simple, Priority.Explicit)
        )

        assertEquals(
            listOf(
                createNoun("i").withMeaning("dog"),
                createIntransVerb("oto").withMeaning("sleep")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(SourcedCategoryValue(TenseValue.Past, CategorySource.Self, tenseSourcedCategory)))
                    )
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Adjectives with Tense pick up it from context`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up tense
        val tenseCategory = Tense(
            listOf(TenseValue.Present, TenseValue.Past),
            setOf(
                PSpeechPart(SpeechPart.Adjective, CategorySource.Self)
            ),
            setOf(SpeechPart.Adjective)
        )
        val tenseSourcedCategory = SourcedCategory(
            tenseCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val tenseExponenceCluster = ExponenceCluster(listOf(tenseSourcedCategory))
        // Set up WordChangeParadigm
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val adjectiveSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Adjective.toDefault(),
            listOf(tenseExponenceCluster),
            mapOf(tenseExponenceCluster to tenseExponenceCluster.possibleValues.zip(tenseApplicators).toMap()),
            ProsodyChangeParadigm(StressType.None)
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Adjective.toDefault() to adjectiveSpeechPartChangeParadigm,
                SpeechPart.Verb.toIntransitive() to SpeechPartChangeParadigm(SpeechPart.Verb.toIntransitive(), listOf(), mapOf(), ProsodyChangeParadigm(StressType.None)),
                SpeechPart.Noun.toDefault() to SpeechPartChangeParadigm(SpeechPart.Noun.toDefault(), listOf(), mapOf(), ProsodyChangeParadigm(StressType.None)),
            )
        )
        val lexis = Lexis(
            listOf(
                createWord("a", SpeechPart.Adjective).withMeaning("new"),
                createNoun("i").withMeaning("dog"),
                createIntransVerb("o").withMeaning("sleep")
            ),
            mapOf(),
            mapOf()
        )
        val language = Language(
            lexis,
            testPhonemeContainer,
            StressType.Initial,
            RestrictionsParadigm(mutableMapOf()),
            DerivationParadigm(listOf(), listOf()),
            ChangeParadigm(
                WordOrder(
                    mapOf(
                        VerbSentenceType.MainVerbClause to
                                SovOrder(listOf(listOf(SyntaxRelation.Agent, SyntaxRelation.Verb).toSampleSpaceObject(1.0)), "Name")),
                    mapOf(),
                    NominalGroupOrder.DNP
                ),
                wordChangeParadigm,
                SyntaxParadigm(
                    CopulaPresence(listOf(io.tashtabash.lang.language.syntax.features.CopulaType.None.toSso(1.0))),
                    QuestionMarkerPresence(null),
                    PredicatePossessionPresence(listOf(io.tashtabash.lang.language.syntax.features.PredicatePossessionType.HaveVerb.toSso(1.0)))
                ),
                NumeralParadigm(NumeralSystemBase.Restricted3, listOf()),
                SyntaxLogic(
                    mapOf(),
                    mapOf(
                        SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>() to SyntaxRelation.Argument to listOf()
                    ),
                    mapOf(),
                    mapOf(),
                    null,
                    mapOf(),
                    mapOf(),
                    listOf(),
                    null
                )
            )
        )
        val sentenceDescription = IntransitiveVerbMainClauseDescription(
            SimpleIntransitiveVerbDescription(
                "sleep",
                NominalDescription(
                    "dog",
                    listOf(AdjectiveDescription("new")),
                    ContextValue.ActorComplimentValue(ContextValue.Amount.AmountValue(3), null)
                )
            )
        )
        val context = Context(
            PrioritizedValue(ContextValue.TimeContext.Past, Priority.Implicit),
            PrioritizedValue(ContextValue.TypeContext.Simple, Priority.Explicit)
        )

        assertEquals(
            listOf(
                createWord("ato", SpeechPart.Adjective)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(SourcedCategoryValue(TenseValue.Past, CategorySource.Self, tenseSourcedCategory)))
                    )
                    .withMeaning("new"),
                createNoun("i").withMeaning("dog"),
                createIntransVerb("o").withMeaning("sleep")
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}

