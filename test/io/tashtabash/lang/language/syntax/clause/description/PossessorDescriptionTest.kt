package io.tashtabash.lang.language.syntax.clause.description

import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.PersonValue.Second
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.SpeechPart.Verb
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.context.*
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import io.tashtabash.lang.language.syntax.context.ContextValue.Amount.AmountValue
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.LongGonePast
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random


internal class PossessorDescriptionTest {
    @Test
    fun `PossessorDescription handles pronoun possessors with simple apposition`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val pronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val noun = createNoun("a") withMeaning "cat"
        val verb = createIntransVerb("do") withMeaning "sleep"
        // Set up WordChangeParadigm
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(
                PersonalPronoun.toDefault() to SpeechPartChangeParadigm(PersonalPronoun.toDefault()),
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive()),
            )
        )
        val language = makeDefLang(
            listOf(pronoun, noun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(Verb.toIntransitive() to Argument to listOf()),
                syntaxRelationSolver = mapOf(Possessor to PersonalPronoun.toDefault() to listOf())
            )
        )
        // Set up descriptions
        val we = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val cat = NominalDescription("cat", ActorComplimentValue(1), listOf(PossessorDescription(we)))
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createWord("o", PersonalPronoun) withMeaning "_personal_pronoun",
                createIntransVerb("do") withMeaning "sleep"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }

    @Test
    fun `PossessorDescription handles pronoun possessors with genitives`() {
        RandomSingleton.safeRandom = Random(Random.nextInt())
        // Set up words
        val pronoun = createWord("o", PersonalPronoun) withMeaning "_personal_pronoun"
        val noun = createNoun("a") withMeaning "cat"
        val verb = createIntransVerb("do") withMeaning "sleep"
        // Set up case
        val caseCategory = Case(
            listOf(CaseValue.Absolutive, CaseValue.Genitive),
            setOf(PersonalPronoun sourcedFrom CategorySource.Self),
            setOf(PersonalPronoun)
        )
        val caseSourcedCategory = SourcedCategory(
            caseCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val caseExponenceCluster = ExponenceCluster(caseSourcedCategory)
        // Set up WordChangeParadigm
        val caseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val pronounChangeParadigm = SpeechPartChangeParadigm(
            PersonalPronoun.toDefault(),
            listOf(caseExponenceCluster to MapApplicatorSource(caseExponenceCluster.possibleValues, caseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(caseCategory),
            mapOf(
                PersonalPronoun.toDefault() to pronounChangeParadigm,
                Noun.toDefault() to SpeechPartChangeParadigm(Noun.toDefault()),
                Verb.toIntransitive() to SpeechPartChangeParadigm(Verb.toIntransitive()),
            )
        )
        val language = makeDefLang(
            listOf(pronoun, noun, verb),
            wordChangeParadigm,
            syntaxLogic = SyntaxLogic(
                verbCasesSolver = mapOf(Verb.toIntransitive() to Argument to listOf()),
                syntaxRelationSolver = mapOf(Possessor to PersonalPronoun.toDefault() to listOf(CaseValue.Genitive))
            )
        )
        // Set up descriptions
        val we = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, NounClassValue.Female, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val cat = NominalDescription("cat", ActorComplimentValue(1), listOf(PossessorDescription(we)))
        val verbDescription = VerbDescription("sleep", mapOf(MainObjectType.Argument to cat))
        val sentenceDescription = VerbMainClauseDescription(verbDescription)
        val context = Context(LongGonePast to Implicit, Indicative to Explicit)

        assertEquals(
            listOf(
                createNoun("a") withMeaning "cat",
                createWord("oto", PersonalPronoun).withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(caseSourcedCategory[CaseValue.Genitive]))
                ) withMeaning "_personal_pronoun",
                createIntransVerb("do") withMeaning "sleep"
            ),
            sentenceDescription.toClause(language, context, Random(Random.nextInt()))
                .unfold(language, Random(Random.nextInt()))
                .words
        )
    }
}

