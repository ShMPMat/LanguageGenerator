package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.paradigm.MapApplicatorSource
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.Number
import io.tashtabash.lang.language.category.PersonValue.Second
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.realization.WordReduplicationCategoryApplicator
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNodeTag
import io.tashtabash.lang.language.syntax.context.*
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import io.tashtabash.lang.language.syntax.context.ContextValue.Amount.AmountValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.LongGonePast
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.*
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.syntax.transformer.*
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class SentenceDescriptionTest {
    @Test
    fun `Intransitive verbs with Tense pick up it from context`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up tense
        val tenseCategory = Tense(
            listOf(TenseValue.Present, TenseValue.Past),
            setOf(Verb sourcedFrom CategorySource.Self),
            setOf(Verb)
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
            Verb.toIntransitive(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "sleep"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(Verb.toIntransitive() to Past to listOf(tenseSourcedCategory[TenseValue.Past])),
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf()
                ),
            )
        )
        val sentenceDescription = VerbMainClauseDescription(
            VerbDescription(
                "sleep",
                mapOf(
                    MainObjectType.Argument to NominalDescription("dog", ActorComplimentValue(3))
                )
            )
        )
        val context = Context(PrioritizedValue(Past, Implicit), PrioritizedValue(Indicative, Explicit))

        assertEquals(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "sleep"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Transitive verbs with Tense pick up it from context`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up tense
        val tenseCategory = Tense(
            listOf(TenseValue.Present, TenseValue.Past),
            setOf(Verb sourcedFrom CategorySource.Self),
            setOf(Verb)
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
            Verb.toDefault(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                Verb.toDefault() to verbSpeechPartChangeParadigm,
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createTransVerb("o") withMeaning "see"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(Verb.toDefault() to Past to listOf(tenseSourcedCategory[TenseValue.Past])),
                verbCasesSolver = mapOf(
                    Verb.toDefault() to SyntaxRelation.Agent to listOf(),
                    Verb.toDefault() to SyntaxRelation.Patient to listOf()
                ),
            )
        )
        val sentenceDescription = VerbMainClauseDescription(
            VerbDescription(
                "see",
                mapOf(
                    MainObjectType.Agent to NominalDescription("dog", ActorComplimentValue(3)),
                    MainObjectType.Patient to NominalDescription("dog", ActorComplimentValue(3))
                )
            )
        )
        val context = Context(PrioritizedValue(Past, Implicit), PrioritizedValue(Indicative, Explicit))

        assertEquals(
            listOf(
                createNoun("i") withMeaning "dog",
                createNoun("i") withMeaning "dog",
                createTransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "see"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Verbs use governance information`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val dog = createNoun("o") withMeaning "dog"
        val verb = createIntransVerb("do")
            .withTags("benefactor", "intrans") withMeaning "sleep"
        // Set up noun class
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Benefactive),
            setOf(Noun sourcedFrom CategorySource.Self),
            setOf(Noun)
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
            Noun.toDefault(),
            listOf(caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(Verb.toIntransitive())
        val wordChangeParadigm = WordChangeParadigm(
            listOf(caseCategory),
            mapOf(
                Noun.toDefault() to nounChangeParadigm,
                Verb.toIntransitive() to intransitiveVerbChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(dog, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(Verb.toIntransitive() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive)),
                verbArgumentSolver = mapOf(// Check if this mapping is used
                    Verb.toIntransitive() to MainObjectType.Agent to SyntaxRelation.Argument,
                    Verb.toIntransitive() to MainObjectType.Patient to SyntaxRelation.Benefactor
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Benefactor to Noun.toDefault() to listOf(CaseValue.Benefactive))
            )
        )
        // Set up descriptions
        val dogDescription = NominalDescription(
            "dog",
            ActorComplimentValue(2, DeixisValue.ProximalAddressee),
        )
        val verbDescription = VerbDescription(
            "sleep",
            mapOf(MainObjectType.Agent to dogDescription, MainObjectType.Patient to dogDescription)
        )
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createWord("oda", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Absolutive]))
                    ) withMeaning "dog",
                createIntransVerb("do").withTags("benefactor", "intrans")
                        withMeaning "sleep",
                createWord("oto", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Benefactive]))
                    ) withMeaning "dog"
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
            setOf(Adjective sourcedFrom CategorySource.Self),
            setOf(Adjective)
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
            Adjective.toDefault(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                Adjective.toDefault() to adjectiveSpeechPartChangeParadigm,
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive()),
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(
                createWord("a", Adjective) withMeaning "new",
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "sleep"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf()
                )
            )
        )
        val sentenceDescription = VerbMainClauseDescription(
            VerbDescription(
                "sleep",
                mapOf(
                    MainObjectType.Argument to NominalDescription(
                        "dog",
                        ActorComplimentValue(3),
                        listOf(AdjectiveDescription("new"))
                    )
                )
            )
        )
        val context = Context(PrioritizedValue(Past, Implicit), PrioritizedValue(Indicative, Explicit))

        assertEquals(
            listOf(
                createWord("ato", Adjective)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
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
        val personalPronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val verb = createIntransVerb("do") withMeaning "sleep"
        // Set up noun class
        val nounClassCategory = NounClass(
            listOf(NounClassValue.Female, NounClassValue.Male),
            setOf(PersonalPronoun sourcedFrom CategorySource.Self),
            setOf(PersonalPronoun)
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
            PersonalPronoun.toDefault(),
            listOf(nounClassExponenceCluster to MapApplicatorSource(nounClassExponenceCluster.possibleValues, nounClassApplicators))
        )
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(Verb.toIntransitive())
        val wordChangeParadigm = WordChangeParadigm(
            listOf(nounClassCategory),
            mapOf(
                PersonalPronoun.toDefault() to personalPronounChangeParadigm,
                Verb.toIntransitive() to intransitiveVerbChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(personalPronoun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf()
                ),
                nounClassCategorySolver = mapOf(NounClassValue.Female to NounClassValue.Female)
            )
        )
        // Set up descriptions
        val personalPronounDescription = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to personalPronounDescription))
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createWord("oda", PersonalPronoun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(nounClassSourcedCategory[NounClassValue.Female]))
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
        // Set up case
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Benefactive),
            setOf(Noun sourcedFrom CategorySource.Self),
            setOf(Noun)
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
            Noun.toDefault(),
            listOf(caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(Verb.toIntransitive())
        val wordChangeParadigm = WordChangeParadigm(
            listOf(caseCategory),
            mapOf(
                Noun.toDefault() to nounChangeParadigm,
                Verb.toIntransitive() to intransitiveVerbChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(dog, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive)
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Benefactor to Noun.toDefault() to listOf(CaseValue.Benefactive))
            )
        )
        // Set up descriptions
        val dogDescription = NominalDescription(
            "dog",
            ActorComplimentValue(2, DeixisValue.ProximalAddressee),
        )
        val verbDescription = VerbDescription(
            "sleep",
            mapOf(MainObjectType.Argument to dogDescription, AdjunctType.Benefactor to dogDescription)
        )
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createWord("oda", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Absolutive]))
                    ) withMeaning "dog",
                createIntransVerb("do").withTags("benefactor", "intrans")
                        withMeaning "sleep",
                createWord("oto", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Benefactive]))
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
            setOf(Noun sourcedFrom CategorySource.Self),
            setOf(Noun)
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
            Noun.toDefault(),
            listOf(numberExponenceCluster to MapApplicatorSource(numberExponenceCluster.possibleValues, numberApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(numberCategory),
            mapOf(
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive()),
                Noun.toDefault() to nounSpeechPartChangeParadigm,
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
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf()
                ),
                numberCategorySolver = NumberCategorySolver(
                    mapOf(NumberValue.Singular to 1..1, NumberValue.Plural to 2..Int.MAX_VALUE),
                    NumberValue.Plural
                )
            )
        )
        val sentenceDescription = VerbMainClauseDescription(
            VerbDescription(
                "sleep",
                mapOf(
                    MainObjectType.Argument to NominalDescription(
                        "dog",
                        ActorComplimentValue(3),
                    )
                )
            )
        )
        val context = Context(PrioritizedValue(Past, Implicit), PrioritizedValue(Indicative, Explicit))

        assertEquals(
            listOf(
                createNoun("i").withMorphemes(
                    MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural]), true)
                ) withMeaning "dog",
                createNoun("i").withMorphemes(
                    MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural]), true)
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
            setOf(Noun sourcedFrom CategorySource.Self),
            setOf(Noun)
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
            setOf(Noun sourcedFrom CategorySource.Self),
            setOf(Noun)
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
            Noun.toDefault(),
            listOf(
                numberExponenceCluster to MapApplicatorSource(numberExponenceCluster.possibleValues, numberApplicators),
                caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators)
            )
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(numberCategory),
            mapOf(
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive()),
                Noun.toDefault() to nounSpeechPartChangeParadigm,
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
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive)
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Benefactor to Noun.toDefault() to listOf(CaseValue.Benefactive)),
                numberCategorySolver = NumberCategorySolver(
                    mapOf(NumberValue.Singular to 1..1, NumberValue.Plural to 2..Int.MAX_VALUE),
                    NumberValue.Plural
                )
            )
        )
        val dogDescription = NominalDescription(
            "dog",
            ActorComplimentValue(2, DeixisValue.ProximalAddressee),
        )
        val verbDescription = VerbDescription(
            "sleep",
            mapOf(
                MainObjectType.Argument to dogDescription,
                AdjunctType.Benefactor to dogDescription
            )
        )
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createWord("oda", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural]), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Absolutive]))
                    ) withMeaning "dog",
                createWord("o", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural]), true),
                    ) withMeaning "dog",
                createIntransVerb("do").withTags("benefactor", "intrans")
                        withMeaning "sleep",
                createWord("oto", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural]), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Benefactive]))
                    ) withMeaning "dog",
                createWord("o", Noun)
                    .withMorphemes(
                        MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural]), true),
                    ) withMeaning "dog"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Personal pronouns can be dropped`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val pronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val verb = createIntransVerb("do") withMeaning "sleep"
        // Set up WordChangeParadigm
        val personalPronounChangeParadigm = SpeechPartChangeParadigm(PersonalPronoun.toDefault())
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(Verb.toIntransitive())
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                PersonalPronoun.toDefault() to personalPronounChangeParadigm,
                Verb.toIntransitive() to intransitiveVerbChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(pronoun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf()
                ),
                transformers = listOf(of(Verb) to RelationTransformer(SyntaxRelation.Argument, DropTransformer))
            )
        )
        // Set up descriptions
        val pronounDescription = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to pronounDescription))
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createIntransVerb("do") withMeaning "sleep"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Patient isn't dropped`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val pronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val verb = createTransVerb("do") withMeaning "build"
        // Set up WordChangeParadigm
        val personalPronounChangeParadigm = SpeechPartChangeParadigm(PersonalPronoun.toDefault())
        val intransitiveVerbChangeParadigm = SpeechPartChangeParadigm(Verb.toDefault())
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                PersonalPronoun.toDefault() to personalPronounChangeParadigm,
                Verb.toDefault() to intransitiveVerbChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(pronoun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toDefault() to SyntaxRelation.Agent to listOf(),
                    Verb.toDefault() to SyntaxRelation.Patient to listOf()
                ),
                transformers = listOf(of(Verb) to RelationTransformer(SyntaxRelation.Agent, DropTransformer))
            )
        )
        // Set up descriptions
        val pronounDescription = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val verbDescription = VerbDescription(
            "build",
            mapOf(MainObjectType.Agent to pronounDescription, MainObjectType.Patient to pronounDescription)
        )
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createWord("o", PersonalPronoun) withMeaning "_personal_pronoun",
                createTransVerb("do") withMeaning "build"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Transformers are applied`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val pronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val noun = createNoun("a") withMeaning "cat"
        val verb = createTransVerb("do") withMeaning "build"
        // Set up WordChangeParadigm
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                PersonalPronoun.toDefault() to SpeechPartChangeParadigm(PersonalPronoun.toDefault()),
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Verb.toDefault() to SpeechPartChangeParadigm(Verb.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(pronoun, noun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toDefault() to SyntaxRelation.Agent to listOf(),
                    Verb.toDefault() to SyntaxRelation.Patient to listOf()
                ),
                transformers = listOf(
                    has(SemanticsTag("trans")) + SyntaxRelation.Agent.matches(of(Noun)) + SyntaxRelation.Patient.matches(of(PersonalPronoun))
                            to RemapOrderTransformer(mapOf(SyntaxRelation.Agent to SyntaxRelation.Patient, SyntaxRelation.Patient to SyntaxRelation.Agent))
                )
            )
        )
        // Set up descriptions
        val pronounDescription = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val nounDescription = NominalDescription("cat", ActorComplimentValue(1))
        val verbDescription = VerbDescription(
            "build",
            mapOf(MainObjectType.Agent to nounDescription, MainObjectType.Patient to pronounDescription)
        )
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createWord("o", PersonalPronoun) withMeaning "_personal_pronoun",
                createNoun("a") withMeaning "cat",
                createTransVerb("do") withMeaning "build"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `PutFirstTransformer changes the order`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val pronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val noun = createNoun("a") withMeaning "cat"
        val verb = createTransVerb("do") withMeaning "build"
        // Set up WordChangeParadigm
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                PersonalPronoun.toDefault() to SpeechPartChangeParadigm(PersonalPronoun.toDefault()),
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Verb.toDefault() to SpeechPartChangeParadigm(Verb.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(pronoun, noun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toDefault() to SyntaxRelation.Agent to listOf(),
                    Verb.toDefault() to SyntaxRelation.Patient to listOf()
                ),
                transformers = listOf(
                    has(SyntaxNodeTag.Topic) to PutFirstTransformer(SyntaxRelation.Predicate)
                )
            )
        )
        // Set up descriptions
        val pronounDescription = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val nounDescription = NominalDescription("cat", ActorComplimentValue(1))
        val verbDescription = VerbDescription(
            "build",
            mapOf(MainObjectType.Agent to nounDescription, MainObjectType.Patient to pronounDescription)
        )
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit, MainObjectType.Patient)

        assertEquals(
            listOf(
                createWord("o", PersonalPronoun) withMeaning "_personal_pronoun",
                createNoun("a") withMeaning "cat",
                createTransVerb("do") withMeaning "build"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}

