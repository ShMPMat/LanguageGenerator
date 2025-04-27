package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.PersonValue.Second
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.context.*
import io.tashtabash.lang.language.syntax.context.ContextValue.ActorValue
import io.tashtabash.lang.language.syntax.context.ContextValue.Amount.AmountValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.LongGonePast
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.*
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
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
            setOf(SpeechPart.Verb sourcedFrom CategorySource.Self),
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
        val language = makeDefLang(
            listOf(
                createNoun("i").withMeaning("dog"),
                createIntransVerb("o").withMeaning("sleep")
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(
                    SpeechPart.Verb.toIntransitive() to Past to listOf(SourcedCategoryValue(TenseValue.Past, CategorySource.Self, tenseSourcedCategory))
                ),
                mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Argument to listOf()
                ),
            )
        )
        val sentenceDescription = IntransitiveVerbMainClauseDescription(
            IntransitiveVerbDescription(
                "sleep",
                NominalDescription(
                    "dog",
                    ContextValue.ActorComplimentValue(AmountValue(3), null),
                )
            )
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Simple, Explicit)
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
            setOf(SpeechPart.Adjective sourcedFrom CategorySource.Self),
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
        val language = makeDefLang(
            listOf(
                createWord("a", SpeechPart.Adjective).withMeaning("new"),
                createNoun("i").withMeaning("dog"),
                createIntransVerb("o").withMeaning("sleep")
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>() to SyntaxRelation.Argument to listOf()
                )
            )
        )
        val sentenceDescription = IntransitiveVerbMainClauseDescription(
            IntransitiveVerbDescription(
                "sleep",
                NominalDescription(
                    "dog",
                    ContextValue.ActorComplimentValue(AmountValue(3), null),
                    listOf(AdjectiveDescription("new"))
                )
            )
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Simple, Explicit)
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


    @Test
    fun `Personal pronouns receive the gender from the context`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val personalPronoun = createWord("o", SpeechPart.PersonalPronoun)
            .withMeaning("_personal_pronoun")
        val verb = createIntransVerb("do")
            .withMeaning("sleep")
        // Set up noun class
        val nounClassCategory = NounClass(
            listOf(NounClassValue.Female, NounClassValue.Male),
            setOf(SpeechPart.PersonalPronoun sourcedFrom CategorySource.Self),
            setOf(SpeechPart.PersonalPronoun)
        )
        val nounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val nounClassExponenceCluster = ExponenceCluster(listOf(nounClassSourcedCategory))
        // Set up WordChangeParadigm
        val nounClassApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val personalPronounChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.PersonalPronoun.toDefault(),
            listOf(nounClassExponenceCluster),
            mapOf(nounClassExponenceCluster to nounClassExponenceCluster.possibleValues.zip(nounClassApplicators).toMap()),
            ProsodyChangeParadigm(StressType.None)
        )
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive(),
            listOf(),
            mapOf(),
            ProsodyChangeParadigm(StressType.None)
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(nounClassCategory),
            mapOf(
                SpeechPart.PersonalPronoun.toDefault() to personalPronounChangeParadigm,
                SpeechPart.Verb.toIntransitive() to intransitiveVerbChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(personalPronoun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>() to SyntaxRelation.Argument to listOf()
                ),
                nounClassCategorySolver = mapOf(NounClassValue.Female to NounClassValue.Female)
            )
        )
        // Set up descriptions
        val personalPronounDescription = PronounDescription(
            "_personal_pronoun",
            listOf(),
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null)
        )
        val verbDescription = IntransitiveVerbDescription("sleep", personalPronounDescription)
        val sentenceDescription = IntransitiveVerbMainClauseDescription(verbDescription)
        val context = Context(
            LongGonePast to Implicit,
            Simple to Explicit
        )

        assertEquals(
            listOf(
                createWord("oda", SpeechPart.PersonalPronoun).withMeaning("_personal_pronoun")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(SourcedCategoryValue(NounClassValue.Female, CategorySource.Self, nounClassSourcedCategory)))
                    ),
                createIntransVerb("do").withMeaning("sleep")
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}

