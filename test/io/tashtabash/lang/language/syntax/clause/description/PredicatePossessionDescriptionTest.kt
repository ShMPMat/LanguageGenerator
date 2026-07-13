package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.lexis.toIntransitive
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.VerbFormResolver
import io.tashtabash.lang.language.syntax.clause.construction.PredicatePossessionConstruction.*
import io.tashtabash.lang.language.syntax.context.DescriptionContext
import io.tashtabash.lang.language.syntax.context.ContextValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.Past
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.syntax.rule
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class PredicatePossessionDescriptionTest {
    @Test
    fun `PredicatePossessionDescription uses the have verb`() {
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
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
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
                createTransVerb("o") withMeaning "have"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                VerbFormResolver(
                    rule { Verb + Past be listOf(tenseSourcedCategory[TenseValue.Past]) }
                ),
                verbCasesSolver = mapOf(
                    Verb.toDefault() to SyntaxRelation.Agent to listOf(),
                    Verb.toDefault() to SyntaxRelation.Patient to listOf()
                ),
            ),
            predicatePossessionConstruction = HaveVerb
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(1)),
            NominalDescription("dog", ContextValue.ActorComplimentValue(3))
        )
        val context = DescriptionContext(Past to Implicit)

        assertEquals(
            listOf(
                createNoun("i") withMeaning "dog",
                createNoun("i") withMeaning "dog",
                createTransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "have"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `PredicatePossessionDescription uses the locative oblique`() {
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
        // Set up noun class
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Locative),
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            Noun.toDefault(),
            listOf(caseExponenceCluster to toHandler(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                VerbFormResolver(
                    rule { Verb + Past be listOf(tenseSourcedCategory[TenseValue.Past]) }
                ),
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Location to Noun.toDefault() to listOf(CaseValue.Locative))
            ),
            predicatePossessionConstruction = LocativeOblique
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(1)),
            NominalDescription("dog", ContextValue.ActorComplimentValue(3))
        )
        val context = DescriptionContext(Past to Implicit)

        assertEquals(
            listOf(
                createNoun("ida").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Absolutive]))
                ) withMeaning "dog",
                createIntransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "exist",
                createNoun("ito").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Locative]))
                ) withMeaning "dog"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `PredicatePossessionDescription uses the dative oblique`() {
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
        // Set up noun class
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Dative),
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            Noun.toDefault(),
            listOf(caseExponenceCluster to toHandler(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                VerbFormResolver(
                    rule { Verb + Past be listOf(tenseSourcedCategory[TenseValue.Past]) }
                ),
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Addressee to Noun.toDefault() to listOf(CaseValue.Dative))
            ),
            predicatePossessionConstruction = DativeOblique
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(1)),
            NominalDescription("dog", ContextValue.ActorComplimentValue(3))
        )
        val context = DescriptionContext(Past to Implicit)

        assertEquals(
            listOf(
                createNoun("ito").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Dative]))
                ) withMeaning "dog",
                createNoun("ida").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Absolutive]))
                ) withMeaning "dog",
                createIntransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "exist"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `PredicatePossessionDescription uses the genitive oblique`() {
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
        // Set up noun class
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Genitive),
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            Noun.toDefault(),
            listOf(caseExponenceCluster to toHandler(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                VerbFormResolver(
                    rule { Verb + Past be listOf(tenseSourcedCategory[TenseValue.Past]) }
                ),
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Possessor to Noun.toDefault() to listOf(CaseValue.Genitive))
            ),
            predicatePossessionConstruction = GenitiveOblique
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(1)),
            NominalDescription("dog", ContextValue.ActorComplimentValue(3))
        )
        val context = DescriptionContext(Past to Implicit)

        assertEquals(
            listOf(
                createNoun("ida").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Absolutive]))
                ) withMeaning "dog",
                createNoun("ito").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Genitive]))
                ) withMeaning "dog",
                createIntransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "exist"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `PredicatePossessionDescription uses the topic`() {
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
        // Set up noun class
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Topic),
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            listOf(tenseExponenceCluster to toHandler(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            Noun.toDefault(),
            listOf(caseExponenceCluster to toHandler(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                VerbFormResolver(
                    rule { Verb + Past be listOf(tenseSourcedCategory[TenseValue.Past]) }
                ),
                verbCasesSolver = mapOf(
                    Verb.toIntransitive() to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Topic to Noun.toDefault() to listOf(CaseValue.Topic))
            ),
            predicatePossessionConstruction = Topic
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(1)),
            NominalDescription("dog", ContextValue.ActorComplimentValue(3))
        )
        val context = DescriptionContext(Past to Implicit)

        assertEquals(
            listOf(
                createNoun("ito").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Topic]))
                ) withMeaning "dog",
                createNoun("ida").withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Absolutive]))
                ) withMeaning "dog",
                createIntransVerb("oto")
                    .withMorphemes(
                        MorphemeData(1, listOf(), true),
                        MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Past]))
                    ) withMeaning "exist"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}
