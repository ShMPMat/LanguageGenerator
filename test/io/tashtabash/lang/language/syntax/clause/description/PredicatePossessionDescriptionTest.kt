package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.paradigm.MapApplicatorSource
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.lexis.toIntransitive
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue
import io.tashtabash.lang.language.syntax.context.ContextValue.Amount.AmountValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.Past
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.Indicative
import io.tashtabash.lang.language.syntax.context.PrioritizedValue
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.syntax.features.PredicatePossessionType
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
            SpeechPart.Verb.toDefault(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Verb.toDefault() to verbSpeechPartChangeParadigm,
                SpeechPart.Noun.toDefault() to SpeechPartChangeParadigm(SpeechPart.Noun.toDefault()),
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createTransVerb("o") withMeaning "have"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(
                    SpeechPart.Verb.toDefault() to Past to listOf(tenseSourcedCategory[TenseValue.Past])
                ),
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toDefault() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Agent to listOf(),
                    SpeechPart.Verb.toDefault() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Patient to listOf()
                ),
            ),
            predicatePossessionType = PredicatePossessionType.HaveVerb
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(1))),
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(3)))
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Indicative, Explicit)
        )

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
            setOf(SpeechPart.Verb sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Verb)
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                SpeechPart.Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(
                    SpeechPart.Verb.toIntransitive() to Past to listOf(tenseSourcedCategory[TenseValue.Past])
                ),
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Location to SpeechPart.Noun.toDefault() to listOf(CaseValue.Locative))
            ),
            predicatePossessionType = PredicatePossessionType.LocativeOblique
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(1))),
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(3)))
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Indicative, Explicit)
        )

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
            setOf(SpeechPart.Verb sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Verb)
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                SpeechPart.Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(
                    SpeechPart.Verb.toIntransitive() to Past to listOf(tenseSourcedCategory[TenseValue.Past])
                ),
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Addressee to SpeechPart.Noun.toDefault() to listOf(CaseValue.Dative))
            ),
            predicatePossessionType = PredicatePossessionType.DativeOblique
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(1))),
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(3)))
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Indicative, Explicit)
        )

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
            setOf(SpeechPart.Verb sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Verb)
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                SpeechPart.Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(
                    SpeechPart.Verb.toIntransitive() to Past to listOf(tenseSourcedCategory[TenseValue.Past])
                ),
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Possessor to SpeechPart.Noun.toDefault() to listOf(CaseValue.Genitive))
            ),
            predicatePossessionType = PredicatePossessionType.GenitiveOblique
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(1))),
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(3)))
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Indicative, Explicit)
        )

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
            setOf(SpeechPart.Verb sourcedFrom CategorySource.Self),
            setOf(SpeechPart.Verb)
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
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val verbSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Verb.toIntransitive(),
            listOf(tenseExponenceCluster to MapApplicatorSource(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val nounChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Verb.toIntransitive() to verbSpeechPartChangeParadigm,
                SpeechPart.Noun.toDefault() to nounChangeParadigm,
            )
        )
        val language = makeDefLang(
            listOf(
                createNoun("i") withMeaning "dog",
                createIntransVerb("o") withMeaning "exist"
            ),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                mapOf(
                    SpeechPart.Verb.toIntransitive() to Past to listOf(tenseSourcedCategory[TenseValue.Past])
                ),
                verbCasesSolver = mapOf(
                    SpeechPart.Verb.toIntransitive() to setOf<CategoryValue>(TenseValue.Past) to SyntaxRelation.Argument to listOf(CaseValue.Absolutive),
                ),
                syntaxRelationSolver = mapOf(SyntaxRelation.Topic to SpeechPart.Noun.toDefault() to listOf(CaseValue.Topic))
            ),
            predicatePossessionType = PredicatePossessionType.Topic
        )
        val sentenceDescription = PredicatePossessionDescription(
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(1))),
            NominalDescription("dog", ContextValue.ActorComplimentValue(AmountValue(3)))
        )
        val context = Context(
            PrioritizedValue(Past, Implicit),
            PrioritizedValue(Indicative, Explicit)
        )

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
