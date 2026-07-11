package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.Case
import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.CategorySource.Agreement
import io.tashtabash.lang.language.category.DeixisValue
import io.tashtabash.lang.language.category.Mood
import io.tashtabash.lang.language.category.MoodValue
import io.tashtabash.lang.language.category.NounClassValue
import io.tashtabash.lang.language.category.Person
import io.tashtabash.lang.language.category.PersonValue.*
import io.tashtabash.lang.language.category.Tense
import io.tashtabash.lang.language.category.TenseValue
import io.tashtabash.lang.language.category.moodName
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.sourcedFrom
import io.tashtabash.lang.language.category.tenseName
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.SimpleWordPointer
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.nominals
import io.tashtabash.lang.language.lexis.toAux
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.lexis.toIntransitive
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.StaticOrder
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.construction.AddAdverb
import io.tashtabash.lang.language.syntax.clause.construction.Auxiliary
import io.tashtabash.lang.language.syntax.clause.construction.SerialAuxiliary
import io.tashtabash.lang.language.syntax.context.DescriptionContext
import io.tashtabash.lang.language.syntax.context.ContextValue.ActorComplimentValue
import io.tashtabash.lang.language.syntax.context.ContextValue.ActorValue
import io.tashtabash.lang.language.syntax.context.ContextValue.Amount.AmountValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.LongGonePast
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import io.tashtabash.lang.language.syntax.context.Priority.*
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class PotentialSentenceTest {
    @Test
    fun `Potential sentence handles potential mood`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val noun = createNoun("a") withMeaning "cat"
        val verb = createIntransVerb("do") withMeaning "sleep"
        // Set up mood
        val moodCategory = Mood(
            listOf(MoodValue.Indicative, MoodValue.Potential),
            setOf(Verb sourcedFrom CategorySource.Self),
            setOf(Verb)
        )
        val moodSourcedCategory = SourcedCategory(
            moodCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val moodExponenceCluster = ExponenceCluster(moodSourcedCategory)
        // Set up WordChangeParadigm
        val moodApplicators = listOf(PassingCategoryApplicator, createAffixCategoryApplicator("-to"))
        val verbChangeParadigm = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            listOf(moodExponenceCluster to toHandler(moodExponenceCluster.possibleValues, moodApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(moodCategory),
            mapOf(
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Verb.toIntransitive() to verbChangeParadigm
            )
        )
        val language = makeDefLang(
            listOf(noun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbFormSolver = mapOf(
                    Verb.toIntransitive() to (null to Potential) to listOf(
                        verbChangeParadigm.getValue(moodName)[MoodValue.Potential],
                    ),
                ),
                verbCasesSolver = mapOf(Verb.toIntransitive() to Argument to listOf()),
            ),
        )
        // Set up descriptions
        val cat = NominalDescription("cat", ActorComplimentValue(1))
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = DescriptionContext(LongGonePast to Implicit, listOf(Potential to Explicit))

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createIntransVerb("doto").withMorphemes(
                    MorphemeData(2, listOf(), true),
                    MorphemeData(2, listOf(moodSourcedCategory[MoodValue.Potential]))
                ) withMeaning "sleep"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Potential sentence handles adverbs`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val noun = createNoun("a") withMeaning "cat"
        val verb = createIntransVerb("do") withMeaning "sleep"
        val adverb = createWord("lah", Adverb) withMeaning "be.able"
        // Set up WordChangeParadigm
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Adverb.toDefault() to SpeechPartChangeParadigm(Adverb.toDefault()),
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive())
            )
        )
        val language = makeDefLang(
            Lexis(listOf(noun, verb, adverb), mapOf(AddAdverb("be.able") to SimpleWordPointer(adverb))),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(Verb.toIntransitive() to Argument to listOf()),
                verbConstructions = mapOf(Verb.toIntransitive() to (null to Potential) to AddAdverb("be.able"))
            ),
        )
        // Set up descriptions
        val cat = NominalDescription("cat", ActorComplimentValue(1))
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = DescriptionContext(LongGonePast to Implicit, listOf(Potential to Explicit))

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createIntransVerb("do") withMeaning "sleep",
                createWord("lah", Adverb) withMeaning "be.able",
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Potential sentence handles aux with trans and intrans`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val noun = createNoun("a") withMeaning "cat"
        val verbIntrans = createIntransVerb("do") withMeaning "sleep"
        val verbTrans = createTransVerb("da") withMeaning "see"
        val aux = createWord("lah", Verb.toAux()) withMeaning "can"
        // Set up WordChangeParadigm
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Verb.toAux() to SpeechPartChangeParadigm(Verb.toAux()),
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive()),
                Verb.toDefault() to SpeechPartChangeParadigm(Verb.toDefault())
            )
        )
        val auxConstruction = SerialAuxiliary(RelationArranger(StaticOrder(Predicate, Auxiliary)))
        val language = makeDefLang(
            Lexis(listOf(noun, verbIntrans, verbTrans, aux), mapOf(auxConstruction to SimpleWordPointer(aux))),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to Argument to listOf(),
                    Verb.toDefault() to Agent to listOf(),
                    Verb.toDefault() to Patient to listOf()
                ),
                verbConstructions = mapOf(
                    Verb.toIntransitive() to (null to Potential) to auxConstruction,
                    Verb.toDefault() to (null to Potential) to auxConstruction,
                )
            ),
        )
        // Set up descriptions
        val cat = NominalDescription("cat", ActorComplimentValue(1))
        val intransVerbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val intransSentenceDescription = VerbMainClauseDescription(intransVerbDescription)
        val context = DescriptionContext(LongGonePast to Implicit, listOf(Potential to Explicit))

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createIntransVerb("do") withMeaning "sleep",
                createWord("lah", Verb.toAux()) withMeaning "can",
            ),
            intransSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )

        val transVerbDescription = VerbDescription("see", mapOf(MainObjectType.Agent to cat, MainObjectType.Patient to cat))
        val transSentenceDescription = VerbMainClauseDescription(transVerbDescription)

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createNoun("a") withMeaning "cat",
                createTransVerb("da") withMeaning "see",
                createWord("lah", Verb.toAux()) withMeaning "can",
            ),
            transSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Potential construction handle cases correctly`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val noun = createNoun("a") withMeaning "cat"
        val verbIntrans = createIntransVerb("do") withMeaning "sleep"
        val verbTrans = createTransVerb("da") withMeaning "see"
        val aux = createWord("lah", Verb.toAux()) withMeaning "can"
        // Set up noun case
        val caseCategory = Case(
            listOf(CaseValue.Nominative, CaseValue.Accusative),
            setOf(Noun sourcedFrom CategorySource.Self),
            setOf(Noun)
        )
        val caseSourcedCategory = SourcedCategory(caseCategory, CategorySource.Self, CompulsoryData(true))
        val caseExponenceCluster = ExponenceCluster(caseSourcedCategory)
        // Set up WordChangeParadigm
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            Noun.toDefault(),
            listOf(caseExponenceCluster to toHandler(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                Noun.toDefault() to nounChangeParadigm,
                Verb.toAux() to SpeechPartChangeParadigm(Verb.toAux()),
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive()),
                Verb.toDefault() to SpeechPartChangeParadigm(Verb.toDefault())
            )
        )
        val auxConstruction = SerialAuxiliary(RelationArranger(StaticOrder(Predicate, Auxiliary)))
        val language = makeDefLang(
            Lexis(listOf(noun, verbIntrans, verbTrans, aux), mapOf(auxConstruction to SimpleWordPointer(aux))),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to Argument to listOf(CaseValue.Nominative),
                    Verb.toDefault() to Agent to listOf(CaseValue.Nominative),
                    Verb.toDefault() to Patient to listOf(CaseValue.Accusative),
                    Verb.toAux() to Argument to listOf(CaseValue.Nominative),
                    Verb.toAux() to Agent to listOf(CaseValue.Nominative),
                    Verb.toAux() to Patient to listOf(CaseValue.Accusative)
                ),
                verbConstructions = mapOf(
                    Verb.toIntransitive() to (null to Potential) to auxConstruction,
                    Verb.toDefault() to (null to Potential) to auxConstruction,
                )
            ),
        )
        // Set up descriptions
        val cat = NominalDescription("cat", ActorComplimentValue(1))
        val intransVerbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val intransSentenceDescription = VerbMainClauseDescription(intransVerbDescription)
        val context = DescriptionContext(LongGonePast to Implicit, listOf(Potential to Explicit))

        assertEquals(
            listOf(
                createNoun("ada")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Nominative]))
                    ) withMeaning "cat",
                createIntransVerb("do") withMeaning "sleep",
                createWord("lah", Verb.toAux()) withMeaning "can",
            ),
            intransSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )

        val transVerbDescription = VerbDescription("see", mapOf(MainObjectType.Agent to cat, MainObjectType.Patient to cat))
        val transSentenceDescription = VerbMainClauseDescription(transVerbDescription)

        assertEquals(
            listOf(
                createNoun("ada")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Nominative]))
                    ) withMeaning "cat",
                createNoun("ato")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Accusative]))
                    ) withMeaning "cat",
                createTransVerb("da") withMeaning "see",
                createWord("lah", Verb.toAux()) withMeaning "can",
            ),
            transSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Potential sentence agrees with subject in serial verb construction`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val pronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val verbIntrans = createIntransVerb("do") withMeaning "sleep"
        val verbTrans = createTransVerb("da") withMeaning "see"
        val aux = createWord("lah", Verb.toAux()) withMeaning "can"
        // Set up verb person agreement
        val personCategory = Person(
            listOf(First, Second, Third),
            setOf(Verb sourcedFrom Agreement(listOf(Agent, Argument), nominals)),
            setOf(Verb)
        )
        val personSourcedCategory = SourcedCategory(personCategory, Agreement(listOf(Agent, Argument), nominals), CompulsoryData(true))
        val personExponenceCluster = ExponenceCluster(personSourcedCategory)
        // Set up WordChangeParadigm
        val personApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"), createAffixCategoryApplicator("-ta"))
        val transVerbChangeParadigm = SpeechPartChangeParadigm(
            Verb.toDefault(),
            listOf(personExponenceCluster to toHandler(personExponenceCluster.possibleValues, personApplicators))
        )
        val intransVerbChangeParadigm = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            listOf(personExponenceCluster to toHandler(personExponenceCluster.possibleValues, personApplicators))
        )
        val auxVerbChangeParadigm = SpeechPartChangeParadigm(
            Verb.toAux(),
            listOf(personExponenceCluster to toHandler(personExponenceCluster.possibleValues, personApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                PersonalPronoun.toDefault() to SpeechPartChangeParadigm(PersonalPronoun.toDefault()),
                Verb.toAux() to auxVerbChangeParadigm,
                Verb.toIntransitive() to intransVerbChangeParadigm,
                Verb.toDefault() to transVerbChangeParadigm
            )
        )
        val auxConstruction = SerialAuxiliary(RelationArranger(StaticOrder(Predicate, Auxiliary)))
        val language = makeDefLang(
            Lexis(listOf(pronoun, verbIntrans, verbTrans, aux), mapOf(auxConstruction to SimpleWordPointer(aux))),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to Argument to listOf(),
                    Verb.toDefault() to Agent to listOf(),
                    Verb.toDefault() to Patient to listOf(),
                    Verb.toAux() to Argument to listOf(),
                    Verb.toAux() to Agent to listOf(),
                    Verb.toAux() to Patient to listOf()
                ),
                verbConstructions = mapOf(
                    Verb.toIntransitive() to (null to Potential) to auxConstruction,
                    Verb.toDefault() to (null to Potential) to auxConstruction,
                )
            ),
        )
        // Set up descriptions
        val i = PronounDescription(
            "_personal_pronoun",
            ActorValue(First, NounClassValue.Female, AmountValue(1), DeixisValue.Proximal, null),
        )
        val you = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Male, AmountValue(10), DeixisValue.Proximal, null),
        )

        val intransVerbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to i))
        val intransSentenceDescription = VerbMainClauseDescription(intransVerbDescription)
        val context = DescriptionContext(LongGonePast to Implicit, listOf(Potential to Explicit))

        assertEquals(
            listOf(
                createWord("o", PersonalPronoun) withMeaning "_personal_pronoun",
                createIntransVerb("doda")
                    .withMorphemes(
                        MorphemeData(2, listOf(), true),
                        MorphemeData(2, listOf(personSourcedCategory[First]))
                    ) withMeaning "sleep",
                createWord("lahda", Verb.toAux())
                    .withMorphemes(
                        MorphemeData(3, listOf(), true),
                        MorphemeData(2, listOf(personSourcedCategory[First]))
                    )  withMeaning "can",
            ),
            intransSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )

        val transVerbDescription = VerbDescription("see", mapOf(MainObjectType.Agent to you, MainObjectType.Patient to i))
        val transSentenceDescription = VerbMainClauseDescription(transVerbDescription)

        assertEquals(
            listOf(
                createWord("o", PersonalPronoun) withMeaning "_personal_pronoun",
                createWord("o", PersonalPronoun) withMeaning "_personal_pronoun",
                createTransVerb("dato")
                    .withMorphemes(
                        MorphemeData(2, listOf(), true),
                        MorphemeData(2, listOf(personSourcedCategory[Second]))
                    ) withMeaning "see",
                createWord("lahto", Verb.toAux())
                    .withMorphemes(
                        MorphemeData(3, listOf(), true),
                        MorphemeData(2, listOf(personSourcedCategory[Second]))
                    ) withMeaning "can",
            ),
            transSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `Potential sentence handles aux governing tense with trans and intrans`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val noun = createNoun("a") withMeaning "cat"
        val verbIntrans = createIntransVerb("do") withMeaning "sleep"
        val verbTrans = createTransVerb("da") withMeaning "see"
        val aux = createWord("lah", Verb.toAux()) withMeaning "can"
        // Set up tense
        val tenseCategory = Tense(
            listOf(TenseValue.Past, TenseValue.Present),
            setOf(Verb sourcedFrom CategorySource.Self),
            setOf(Verb)
        )
        val tenseSourcedCategory = SourcedCategory(tenseCategory, CategorySource.Self, CompulsoryData(true))
        val tenseExponenceCluster = ExponenceCluster(tenseSourcedCategory)
        // Set up WordChangeParadigm
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val auxVerbChangeParadigm = SpeechPartChangeParadigm(
            Verb.toAux(),
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val intransVerbChangeParadigm = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val transVerbChangeParadigm = SpeechPartChangeParadigm(
            Verb.toDefault(),
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Verb.toAux() to auxVerbChangeParadigm,
                Verb.toIntransitive() to intransVerbChangeParadigm,
                Verb.toDefault() to transVerbChangeParadigm
            )
        )
        val auxConstruction = Auxiliary(RelationArranger(StaticOrder(Predicate, Auxiliary)), listOf(TenseValue.Present))
        val language = makeDefLang(
            Lexis(listOf(noun, verbIntrans, verbTrans, aux), mapOf(auxConstruction to SimpleWordPointer(aux))),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to Argument to listOf(),
                    Verb.toDefault() to Agent to listOf(),
                    Verb.toDefault() to Patient to listOf()
                ),
                verbFormSolver = mapOf(
                    Verb.toIntransitive() to (LongGonePast to null) to listOf(intransVerbChangeParadigm.getValue(tenseName)[TenseValue.Past]),
                    Verb.toAux() to (LongGonePast to null) to listOf(intransVerbChangeParadigm.getValue(tenseName)[TenseValue.Past]),
                    Verb.toDefault() to (LongGonePast to null) to listOf(intransVerbChangeParadigm.getValue(tenseName)[TenseValue.Past])
                ),
                verbConstructions = mapOf(
                    Verb.toIntransitive() to (null to Potential) to auxConstruction,
                    Verb.toDefault() to (null to Potential) to auxConstruction,
                )
            ),
        )
        // Set up descriptions
        val cat = NominalDescription("cat", ActorComplimentValue(1))
        val intransVerbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val intransSentenceDescription = VerbMainClauseDescription(intransVerbDescription)
        val context = DescriptionContext(LongGonePast to Implicit, listOf(Potential to Explicit))

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createIntransVerb("doto")
                    .withMorphemes(
                        MorphemeData(2, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Present]))
                    ) withMeaning "sleep",
                createWord("lahda", Verb.toAux())
                    .withMorphemes(
                        MorphemeData(3, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "can",
            ),
            intransSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )

        val transVerbDescription = VerbDescription("see", mapOf(MainObjectType.Agent to cat, MainObjectType.Patient to cat))
        val transSentenceDescription = VerbMainClauseDescription(transVerbDescription)

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createNoun("a") withMeaning "cat",
                createTransVerb("dato")
                    .withMorphemes(
                        MorphemeData(2, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Present]))
                    ) withMeaning "see",
                createWord("lahda", Verb.toAux())
                    .withMorphemes(
                        MorphemeData(3, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "can"
            ),
            transSentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}

