package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.Number
import io.tashtabash.lang.language.category.PersonValue.Second
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.realization.WordReduplicationCategoryApplicator
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
        val tenseExponenceCluster = ExponenceCluster(tenseSourcedCategory)
        // Set up WordChangeParadigm
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive(),
            listOf(tenseExponenceCluster),
            mapOf(tenseExponenceCluster to tenseExponenceCluster.possibleValues.zip(tenseApplicators).toMap())
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                SpeechPart.Noun.toDefault() to SpeechPartChangeParadigm(SpeechPart.Noun.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "sleep"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(
                    SpeechPart.Verb.toIntransitive() to Past to listOf(tenseSourcedCategory.getValue(TenseValue.Past))
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
                    ContextValue.ActorComplimentValue(AmountValue(3)),
                )
            )
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Simple, Explicit)
        )

        assertEquals(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory.getValue(TenseValue.Past)))
                    ) withMeaning "sleep"
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
        val tenseExponenceCluster = ExponenceCluster(tenseSourcedCategory)
        // Set up WordChangeParadigm
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val adjectiveSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Adjective.toDefault(),
            listOf(tenseExponenceCluster),
            mapOf(tenseExponenceCluster to tenseExponenceCluster.possibleValues.zip(tenseApplicators).toMap())
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Adjective.toDefault() to adjectiveSpeechPartChangeParadigm,
                SpeechPart.Verb.toIntransitive() to SpeechPartChangeParadigm(SpeechPart.Verb.toIntransitive()),
                SpeechPart.Noun.toDefault() to SpeechPartChangeParadigm(SpeechPart.Noun.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(
                createWord("a", SpeechPart.Adjective) withMeaning "new",
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "sleep"
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
                    ContextValue.ActorComplimentValue(AmountValue(3)),
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
                        MorphemeData(2, listOf(tenseSourcedCategory.getValue(TenseValue.Past)))
                    ) withMeaning "new",
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "sleep"
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
        val personalPronoun = createWord("o", SpeechPart.PersonalPronoun) withMeaning "_personal_pronoun"
        val verb = createIntransVerb("do") withMeaning "sleep"
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
        val nounClassExponenceCluster = ExponenceCluster(nounClassSourcedCategory)
        // Set up WordChangeParadigm
        val nounClassApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val personalPronounChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.PersonalPronoun.toDefault(),
            listOf(nounClassExponenceCluster),
            mapOf(nounClassExponenceCluster to nounClassExponenceCluster.possibleValues.zip(nounClassApplicators).toMap())
        )
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive()
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
                createWord("oda", SpeechPart.PersonalPronoun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(nounClassSourcedCategory.getValue(NounClassValue.Female)))
                    ) withMeaning "_personal_pronoun",
                createIntransVerb("do") withMeaning "sleep"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Beneficiary case is resolved`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val dog = createNoun("o") withMeaning "dog"
        val verb = createIntransVerb("do")
            .withTags("benefactor", "intrans") withMeaning "sleep"
        // Set up noun class
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Benefactive),
            setOf(SpeechPart.Noun sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Noun)
        )
        val caseSourcedCategory = SourcedCategory(
            caseCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val caseExponenceCluster = ExponenceCluster(caseSourcedCategory)
        // Set up WordChangeParadigm
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(caseExponenceCluster),
            mapOf(caseExponenceCluster to caseExponenceCluster.possibleValues.zip(caseApplicators).toMap())
        )
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive()
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(caseCategory),
            mapOf(
                SpeechPart.Noun.toDefault() to nounChangeParadigm,
                SpeechPart.Verb.toIntransitive() to intransitiveVerbChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(dog, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive)
                ),
                nonCoreCaseSolver = mapOf(CaseValue.Benefactive to SpeechPart.Noun.toDefault() to listOf(CaseValue.Benefactive))
            )
        )
        // Set up descriptions
        val dogDescription = NominalDescription(
            "dog",
            ContextValue.ActorComplimentValue(AmountValue(2), DeixisValue.ProximalAddressee),
        )
        val verbDescription = IntransitiveVerbDescription(
            "sleep",
            dogDescription,
            listOf(IndirectObjectDescription(dogDescription, IndirectObjectType.Benefactor))
        )
        val sentenceDescription = IntransitiveVerbMainClauseDescription(verbDescription)
        val context = Context(
            LongGonePast to Implicit,
            Simple to Explicit
        )

        assertEquals(
            listOf(
                createWord("oda", SpeechPart.Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory.getValue(CaseValue.Absolutive)))
                    ) withMeaning "dog",
                createIntransVerb("do").withTags("benefactor", "intrans")
                        withMeaning "sleep",
                createWord("oto", SpeechPart.Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory.getValue(CaseValue.Benefactive)))
                    ) withMeaning "dog"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Reduplicated plurals work`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up tense
        val numberCategory = Number(
            listOf(NumberValue.Singular, NumberValue.Plural),
            setOf(SpeechPart.Noun sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Noun)
        )
        val numberSourcedCategory = SourcedCategory(
            numberCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val numberExponenceCluster = ExponenceCluster(numberSourcedCategory)
        // Set up WordChangeParadigm
        val numberApplicators = listOf(PassingCategoryApplicator, WordReduplicationCategoryApplicator())
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(numberExponenceCluster),
            mapOf(numberExponenceCluster to numberExponenceCluster.possibleValues.zip(numberApplicators).toMap())
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(numberCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to SpeechPartChangeParadigm(SpeechPart.Verb.toIntransitive()),
                SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "sleep"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>() to SyntaxRelation.Argument to listOf()
                ),
                numberCategorySolver = NumberCategorySolver(
                    mapOf(NumberValue.Singular to 1..1, NumberValue.Plural to 2..Int.MAX_VALUE),
                    NumberValue.Plural
                )
            )
        )
        val sentenceDescription = IntransitiveVerbMainClauseDescription(
            IntransitiveVerbDescription(
                "sleep",
                NominalDescription(
                    "dog",
                    ContextValue.ActorComplimentValue(AmountValue(3)),
                )
            )
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Simple, Explicit)
        )

        assertEquals(
            listOf(
                createNoun("i").withMorphemes(
                    MorphemeData(1, listOf(numberSourcedCategory.getValue(NumberValue.Plural)), true)
                ) withMeaning "dog",
                createNoun("i").withMorphemes(
                    MorphemeData(1, listOf(numberSourcedCategory.getValue(NumberValue.Plural)), true)
                ) withMeaning "dog",
                createIntransVerb("o") withMeaning "sleep"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Reduplicated plurals work even if reduplication isn't the last operation`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up number
        val numberCategory = Number(
            listOf(NumberValue.Singular, NumberValue.Plural),
            setOf(SpeechPart.Noun sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Noun)
        )
        val numberSourcedCategory = SourcedCategory(
            numberCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val numberExponenceCluster = ExponenceCluster(numberSourcedCategory)
        // Set up case
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Benefactive),
            setOf(SpeechPart.Noun sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Noun)
        )
        val caseSourcedCategory = SourcedCategory(
            caseCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val caseExponenceCluster = ExponenceCluster(caseSourcedCategory)
        // Set up WordChangeParadigm
        val numberApplicators = listOf(PassingCategoryApplicator, WordReduplicationCategoryApplicator())
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(numberExponenceCluster, caseExponenceCluster),
            mapOf(
                numberExponenceCluster to numberExponenceCluster.possibleValues.zip(numberApplicators).toMap(),
                caseExponenceCluster to caseExponenceCluster.possibleValues.zip(caseApplicators).toMap()
            )
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(numberCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to SpeechPartChangeParadigm(SpeechPart.Verb.toIntransitive(), listOf(), mapOf(), ProsodyChangeParadigm(StressType.None)),
                SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("o") withMeaning "dog",
                createIntransVerb("do")
                    .withTags("benefactor", "intrans") withMeaning "sleep"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                numberCategorySolver = NumberCategorySolver(
                    mapOf(NumberValue.Singular to 1..1, NumberValue.Plural to 2..Int.MAX_VALUE),
                    NumberValue.Plural
                ),
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive)
                ),
                nonCoreCaseSolver = mapOf(CaseValue.Benefactive to SpeechPart.Noun.toDefault() to listOf(CaseValue.Benefactive))
            )
        )
        val dogDescription = NominalDescription(
            "dog",
            ContextValue.ActorComplimentValue(AmountValue(2), DeixisValue.ProximalAddressee),
        )
        val verbDescription = IntransitiveVerbDescription(
            "sleep",
            dogDescription,
            listOf(IndirectObjectDescription(dogDescription, IndirectObjectType.Benefactor))
        )
        val sentenceDescription = IntransitiveVerbMainClauseDescription(verbDescription)
        val context = Context(
            LongGonePast to Implicit,
            Simple to Explicit
        )

        assertEquals(
            listOf(
                createWord("oda", SpeechPart.Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory.getValue(NumberValue.Plural)), true),
                        MorphemeData(2, listOf(caseSourcedCategory.getValue(CaseValue.Absolutive)))
                    ) withMeaning "dog",
                createWord("o", SpeechPart.Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory.getValue(NumberValue.Plural)), true),
                    ) withMeaning "dog",
                createIntransVerb("do").withTags("benefactor", "intrans")
                        withMeaning "sleep",
                createWord("oto", SpeechPart.Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory.getValue(NumberValue.Plural)), true),
                        MorphemeData(2, listOf(caseSourcedCategory.getValue(CaseValue.Benefactive)))
                    ) withMeaning "dog",
                createWord("o", SpeechPart.Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory.getValue(NumberValue.Plural)), true),
                    ) withMeaning "dog"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}

