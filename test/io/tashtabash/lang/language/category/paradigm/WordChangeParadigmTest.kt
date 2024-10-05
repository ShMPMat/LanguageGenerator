package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.realization.SuffixWordCategoryApplicator
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.nominals
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.util.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class WordChangeParadigmTest {
    @Test
    fun `articles can decline by noun class`() {
        // Set up words
        val article = createWord("a", SpeechPart.Article)
        val noun = createNoun("daba")
            .withStaticCategories(NounClassValue.Fruit)
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(
                PSpeechPart(SpeechPart.Noun, CategorySource.Self),
                PSpeechPart(SpeechPart.Article, CategorySource.Self)
            ),
            setOf(SpeechPart.Article)
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            CategorySource.Self,
            CompulsoryData(false)
        )
        val definitenessExponenceCluster = ExponenceCluster(
            listOf(definitenessSourcedCategory),
            definitenessSourcedCategory.actualSourcedValues.map { listOf(it) }.toSet()
        )
        // Set up noun class
        val nounClassCategory = NounClass(
            listOf(NounClassValue.LongObject, NounClassValue.Fruit),
            setOf(
                PSpeechPart(SpeechPart.Noun, CategorySource.Self),
                PSpeechPart(SpeechPart.Article, CategorySource.Agreement(SyntaxRelation.Agent, nominals))
            ),
            setOf(SpeechPart.Noun)
        )
        val articleNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            CategorySource.Agreement(SyntaxRelation.Agent, nominals),
            CompulsoryData(true)
        )
        val articleNounClassExponenceCluster = ExponenceCluster(
            listOf(articleNounClassSourcedCategory),
            articleNounClassSourcedCategory.actualSourcedValues.map { listOf(it) }.toSet()
        )
        val nounNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val nounNounClassExponenceCluster = ExponenceCluster(
            listOf(nounNounClassSourcedCategory),
            nounNounClassSourcedCategory.actualSourcedValues.map { listOf(it) }.toSet()
        )
        // Set up WordChangeParadigm
        val nounDefinitenessApplicators = listOf(SuffixWordCategoryApplicator(article, LatchType.ClauseLatch))
        val nounClassApplicatiors = listOf(PassingCategoryApplicator, PassingCategoryApplicator)
        val articleApplicators = listOf(
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("p-"), CategoryRealization.Prefix)
        )
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            TypedSpeechPart(SpeechPart.Noun),
            listOf(definitenessExponenceCluster, nounNounClassExponenceCluster),
            mapOf(
                definitenessExponenceCluster to definitenessExponenceCluster.possibleValues.zip(nounDefinitenessApplicators).toMap(),
                nounNounClassExponenceCluster to nounNounClassExponenceCluster.possibleValues.zip(nounClassApplicatiors).toMap()
            ),
            ProsodyChangeParadigm(StressType.None)
        )
        val articleSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            TypedSpeechPart(SpeechPart.Article),
            listOf(articleNounClassExponenceCluster),
            mapOf(articleNounClassExponenceCluster to articleNounClassExponenceCluster.possibleValues.zip(articleApplicators).toMap()),
            ProsodyChangeParadigm(StressType.None)
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(nounClassCategory, definitenessCategory),
            mapOf(
                TypedSpeechPart(SpeechPart.Noun) to nounSpeechPartChangeParadigm,
                TypedSpeechPart(SpeechPart.Article) to articleSpeechPartChangeParadigm
            )
        )

        val result = wordChangeParadigm.apply(
            noun,
            LatchType.Center,
            listOf(definitenessSourcedCategory.actualSourcedValues[0])
        )

        assertEquals(
            listOf(
                noun.withMorphemes(
                    MorphemeData(4, listOf(), true),
                    MorphemeData(0, listOf(SourcedCategoryValue(NounClassValue.Fruit, CategorySource.Self, nounNounClassSourcedCategory)), false)
                ),
                createWord("pa", SpeechPart.Article).withMorphemes(
                    MorphemeData(1, listOf(SourcedCategoryValue(NounClassValue.Fruit, CategorySource.Agreement(SyntaxRelation.Agent, nominals), articleNounClassSourcedCategory)), false),
                    MorphemeData(1, listOf(SourcedCategoryValue(DefinitenessValue.Definite, CategorySource.Self, definitenessSourcedCategory)), true)
                )
            ),
            result.unfold().words
        )
    }
}
