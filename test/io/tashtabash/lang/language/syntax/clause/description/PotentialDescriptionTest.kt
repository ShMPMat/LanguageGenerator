package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.Mood
import io.tashtabash.lang.language.category.MoodValue
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.sourcedFrom
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.SimpleWordPointer
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.lexis.toIntransitive
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation.Argument
import io.tashtabash.lang.language.syntax.clause.construction.PotentialConstruction
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.ActorComplimentValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.LongGonePast
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.Indicative
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class PotentialDescriptionTest {
    @Test
    fun `PotentialDescription handles potential mood`() {
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
            listOf(moodExponenceCluster to MapApplicatorSource(moodExponenceCluster.possibleValues, moodApplicators))
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
                verbCasesSolver = mapOf(Verb.toIntransitive() to Argument to listOf()),
            ),
            potentialConstruction = PotentialConstruction.Mood
        )
        // Set up descriptions
        val cat = NominalDescription("cat", ActorComplimentValue(1))
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val sentenceDescription = PotentialDescription(VerbMainClauseDescription(verbDescription))
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

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
    fun `PotentialDescription handles adverbs`() {
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
            Lexis(listOf(noun, verb, adverb), mapOf(PotentialConstruction.Adverb to SimpleWordPointer(adverb)), mapOf()),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(Verb.toIntransitive() to Argument to listOf()),
            ),
            potentialConstruction = PotentialConstruction.Adverb
        )
        // Set up descriptions
        val cat = NominalDescription("cat", ActorComplimentValue(1))
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val sentenceDescription = PotentialDescription(VerbMainClauseDescription(verbDescription))
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

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
}

