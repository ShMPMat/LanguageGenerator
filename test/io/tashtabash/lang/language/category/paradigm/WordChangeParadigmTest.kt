package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.generator.ValueMap
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.CategorySource.*
import io.tashtabash.lang.language.category.Number
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.realization.SuffixWordCategoryApplicator
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.sequence.toWordSequence
import io.tashtabash.lang.language.util.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class WordChangeParadigmTest {
    @Test
    fun `Articles can decline by noun class`() {
        // Set up words
        val article = createWord("a", SpeechPart.Article)
        val noun = createNoun("daba")
            .withStaticCategories(NounClassValue.Fruit)
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(
                SpeechPart.Noun sourcedFrom Self,
                SpeechPart.Article sourcedFrom Self
            ),
            setOf(SpeechPart.Article)
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            Self,
            CompulsoryData(false)
        )
        val definitenessExponenceCluster = ExponenceCluster(definitenessSourcedCategory)
        // Set up noun class
        val nounClassCategory = NounClass(
            listOf(NounClassValue.LongObject, NounClassValue.Fruit),
            setOf(
                SpeechPart.Noun sourcedFrom Self,
                SpeechPart.Article sourcedFrom Agreement(SyntaxRelation.Agent, nominals)
            ),
            setOf(SpeechPart.Noun)
        )
        val articleNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            Agreement(SyntaxRelation.Agent, nominals),
            CompulsoryData(true)
        )
        val articleNounClassExponenceCluster = ExponenceCluster(articleNounClassSourcedCategory)
        val nounNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            Self,
            CompulsoryData(true)
        )
        val nounNounClassExponenceCluster = ExponenceCluster(nounNounClassSourcedCategory)
        // Set up WordChangeParadigm
        val nounDefinitenessApplicators = listOf(SuffixWordCategoryApplicator(article, LatchType.ClauseLatch))
        val nounClassApplicatiors = listOf(PassingCategoryApplicator, PassingCategoryApplicator)
        val articleApplicators = listOf(createAffixCategoryApplicator("b-"), createAffixCategoryApplicator("p-"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(definitenessExponenceCluster, nounNounClassExponenceCluster),
            mapOf(
                definitenessExponenceCluster to ValueMap(definitenessExponenceCluster.possibleValues, nounDefinitenessApplicators),
                nounNounClassExponenceCluster to ValueMap(nounNounClassExponenceCluster.possibleValues, nounClassApplicatiors)
            )
        )
        val articleSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Article.toDefault(),
            listOf(articleNounClassExponenceCluster),
            mapOf(articleNounClassExponenceCluster to ValueMap(articleNounClassExponenceCluster.possibleValues, articleApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(nounClassCategory, definitenessCategory),
            mapOf(
                SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm,
                SpeechPart.Article.toDefault() to articleSpeechPartChangeParadigm
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
                    MorphemeData(4, listOf(nounNounClassSourcedCategory[NounClassValue.Fruit]), true),
                ),
                createWord("pa", SpeechPart.Article).withMorphemes(
                    MorphemeData(1, listOf(articleNounClassSourcedCategory[NounClassValue.Fruit])),
                    MorphemeData(1, listOf(definitenessSourcedCategory[DefinitenessValue.Definite]), true)
                )
            ),
            result.unfold().words
        )
    }

    @Test
    fun `Adjectives can conjugate by compulsory tense`() {
        // Set up tense
        val tenseCategory = Tense(
            listOf(TenseValue.Present, TenseValue.Past),
            setOf(SpeechPart.Adjective sourcedFrom Self),
            setOf(SpeechPart.Adjective)
        )
        val tenseSourcedCategory = SourcedCategory(
            tenseCategory,
            Self,
            CompulsoryData(true)
        )
        val tenseExponenceCluster = ExponenceCluster(tenseSourcedCategory)
        // Set up WordChangeParadigm
        val tenseApplicators = listOf(createAffixCategoryApplicator("-da"), createAffixCategoryApplicator("-to"))
        val adjectiveSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Adjective.toDefault(),
            listOf(tenseExponenceCluster),
            mapOf(tenseExponenceCluster to ValueMap(tenseExponenceCluster.possibleValues, tenseApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(tenseCategory),
            mapOf(
                SpeechPart.Adjective.toDefault() to adjectiveSpeechPartChangeParadigm,
            )
        )

        val result = wordChangeParadigm.apply(
            createWord("a", SpeechPart.Adjective),
            LatchType.Center,
            listOf(tenseSourcedCategory.actualSourcedValues[0])
        )

        assertEquals(
            listOf(
                createWord("ada", SpeechPart.Adjective).withMorphemes(
                    MorphemeData(1, listOf(), true),
                    MorphemeData(2, listOf(tenseSourcedCategory[TenseValue.Present]))
                )
            ),
            result.unfold().words
        )
    }

    @Test
    fun `Ultimate stress is moved when a suffix is applied`() {
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(SpeechPart.Noun sourcedFrom Self),
            setOf()
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            Self,
            CompulsoryData(false)
        )
        val definitenessExponenceCluster = ExponenceCluster(definitenessSourcedCategory)
        // Set up WordChangeParadigm
        val nounDefinitenessApplicators = listOf(createAffixCategoryApplicator("-dac"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(definitenessExponenceCluster),
            mapOf(
                definitenessExponenceCluster to ValueMap(definitenessExponenceCluster.possibleValues, nounDefinitenessApplicators),
            ),
            ProsodyChangeParadigm(StressType.Ultimate)
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(definitenessCategory),
            mapOf(SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm)
        )

        val result = wordChangeParadigm.apply(
            createNoun("daba").withProsodyOn(1, Prosody.Stress),
            LatchType.Center,
            listOf(definitenessSourcedCategory.actualSourcedValues[0])
        )

        assertEquals(
            listOf(
                createNoun("dabadac")
                    .withProsodyOn(2, Prosody.Stress)
                    .withMorphemes(
                        MorphemeData(4, listOf(), true),
                        MorphemeData(3, listOf(definitenessSourcedCategory[DefinitenessValue.Definite]))
                    )
            ),
            result.unfold().words
        )
    }

    @Test
    fun `Sandhi rules are applied`() {
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault()
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(),
            mapOf(SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm),
            listOf(createTestPhonologicalRule("[+Voiced] -> [-Voiced] / _ $"))
        )

        val result = wordChangeParadigm.apply(
            createNoun("dab"),
            LatchType.Center,
            listOf()
        )

        assertEquals(
            listOf(createNoun("dap")),
            result.unfold().words
        )
    }

    @Test
    fun `Sandhi rules are applied after category changes`() {
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(SpeechPart.Noun sourcedFrom Self)
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            Self,
            CompulsoryData(false)
        )
        val definitenessExponenceCluster = ExponenceCluster(definitenessSourcedCategory)
        // Set up WordChangeParadigm
        val nounDefinitenessApplicators = listOf(createAffixCategoryApplicator("-cad"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(definitenessExponenceCluster),
            mapOf(
                definitenessExponenceCluster to ValueMap(definitenessExponenceCluster.possibleValues, nounDefinitenessApplicators),
            )
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(definitenessCategory),
            mapOf(SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm),
            listOf(createTestPhonologicalRule("[+Voiced] -> [-Voiced] / _ $"))
        )

        val result = wordChangeParadigm.apply(
            createNoun("dab"),
            LatchType.Center,
            listOf(definitenessSourcedCategory.actualSourcedValues[0])
        )

        assertEquals(
            listOf(
                createNoun("dabcat")
                    .withMorphemes(
                        MorphemeData(3, listOf(), true),
                        MorphemeData(3, listOf(definitenessSourcedCategory[DefinitenessValue.Definite]))
                    )
            ),
            result.unfold().words
        )
    }

    @Test
    fun `Sandhi rules handle morpheme boundaries correctly`() {
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(SpeechPart.Noun sourcedFrom Self),
            setOf()
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            Self,
            CompulsoryData(false)
        )
        val definitenessExponenceCluster = ExponenceCluster(definitenessSourcedCategory)
        // Set up WordChangeParadigm
        val nounDefinitenessApplicators = listOf(createAffixCategoryApplicator("-cad"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(definitenessExponenceCluster),
            mapOf(
                definitenessExponenceCluster to ValueMap(definitenessExponenceCluster.possibleValues, nounDefinitenessApplicators),
            )
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(definitenessCategory),
            mapOf(SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm),
            listOf(createTestPhonologicalRule("d -> - / _ $"))
        )

        val result = wordChangeParadigm.apply(
            createNoun("dab"),
            LatchType.Center,
            listOf(definitenessSourcedCategory.actualSourcedValues[0])
        )

        assertEquals(
            listOf(
                createNoun("dabca")
                    .withMorphemes(
                        MorphemeData(3, listOf(), true),
                        MorphemeData(2, listOf(definitenessSourcedCategory[DefinitenessValue.Definite]))
                    )
            ),
            result.unfold().words
        )
    }

    @Test
    fun `getAllWordForms returns all word forms`() {
        // Set up words
        val article = createWord("a", SpeechPart.Article)
            .withStaticCategories(DefinitenessValue.Definite)
        val noun = createNoun("daba")
            .withStaticCategories(NounClassValue.Fruit)
        val lexis = Lexis(listOf(article, noun), mapOf(), mapOf())
            .reifyPointers()
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(
                SpeechPart.Noun sourcedFrom Self,
                SpeechPart.Article sourcedFrom Self
            ),
            setOf(SpeechPart.Article)
        )
        val nounDefinitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            Self,
            CompulsoryData(false)
        )
        val nounDefinitenessExponenceCluster = ExponenceCluster(nounDefinitenessSourcedCategory)
        // Set up noun class
        val nounClassCategory = NounClass(
            listOf(NounClassValue.LongObject, NounClassValue.Fruit),
            setOf(
                SpeechPart.Noun sourcedFrom Self,
                SpeechPart.Article sourcedFrom Agreement(SyntaxRelation.Agent, nominals)
            ),
            setOf(SpeechPart.Noun)
        )
        val articleNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            Agreement(SyntaxRelation.Agent, nominals),
            CompulsoryData(true)
        )
        val articleNounClassExponenceCluster = ExponenceCluster(articleNounClassSourcedCategory)
        val nounNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            Self,
            CompulsoryData(true)
        )
        val nounNounClassExponenceCluster = ExponenceCluster(nounNounClassSourcedCategory)
        // Set up WordChangeParadigm
        val nounDefinitenessApplicators = listOf(SuffixWordCategoryApplicator(article, LatchType.ClauseLatch))
        val nounClassApplicatiors = listOf(PassingCategoryApplicator, PassingCategoryApplicator)
        val articleApplicators = listOf(createAffixCategoryApplicator("b-"), createAffixCategoryApplicator("p-"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(nounDefinitenessExponenceCluster, nounNounClassExponenceCluster),
            mapOf(
                nounDefinitenessExponenceCluster to ValueMap(nounDefinitenessExponenceCluster.possibleValues, nounDefinitenessApplicators),
                nounNounClassExponenceCluster to ValueMap(nounNounClassExponenceCluster.possibleValues, nounClassApplicatiors)
            )
        )
        val articleSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Article.toDefault(),
            listOf(articleNounClassExponenceCluster),
            mapOf(articleNounClassExponenceCluster to ValueMap(articleNounClassExponenceCluster.possibleValues, articleApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(nounClassCategory, definitenessCategory),
            mapOf(
                SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm,
                SpeechPart.Article.toDefault() to articleSpeechPartChangeParadigm
            )
        )

        assertEquals(
            listOf(
                // Noun forms
                noun.withMorphemes(
                    MorphemeData(4, listOf(nounNounClassSourcedCategory[NounClassValue.Fruit]), true),
                ).toWordSequence() to listOf(),
                WordSequence(
                    noun.withMorphemes(
                        MorphemeData(4, listOf(nounNounClassSourcedCategory[NounClassValue.Fruit]), true),
                    ),
                    createWord("pa", SpeechPart.Article).withMorphemes(
                        MorphemeData(1, listOf(articleNounClassSourcedCategory[NounClassValue.Fruit])),
                        MorphemeData(1, listOf(nounDefinitenessSourcedCategory[DefinitenessValue.Definite]), true)
                    ).withStaticCategories(DefinitenessValue.Definite)
                ) to listOf(
                    nounDefinitenessSourcedCategory[DefinitenessValue.Definite]
                ),
                // Article forms
                createWord("pa", SpeechPart.Article).withMorphemes(
                    MorphemeData(1, listOf(articleNounClassSourcedCategory[NounClassValue.Fruit])),
                    MorphemeData(1, listOf(), true)
                ).withStaticCategories(DefinitenessValue.Definite).toWordSequence() to listOf(
                    articleNounClassSourcedCategory[NounClassValue.Fruit]
                ),
                createWord("ba", SpeechPart.Article).withMorphemes(
                    MorphemeData(1, listOf(articleNounClassSourcedCategory[NounClassValue.LongObject])),
                    MorphemeData(1, listOf(), true)
                ).withStaticCategories(DefinitenessValue.Definite).toWordSequence() to listOf(
                    articleNounClassSourcedCategory[NounClassValue.LongObject]
                )
            ).sortedBy { it.toString() },
            wordChangeParadigm.getAllWordForms(lexis, true).sortedBy { it.toString() }
        )
    }

    @Test
    fun `getUniqueWordForms returns unique word forms`() {
        // Set up words
        val article = createWord("a", SpeechPart.Article)
            .withStaticCategories(DefinitenessValue.Definite)
        val noun = createNoun("daba")
            .withStaticCategories(NounClassValue.Fruit)
        val lexis = Lexis(listOf(article, noun), mapOf(), mapOf())
            .reifyPointers()
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(
                SpeechPart.Noun sourcedFrom Self,
                SpeechPart.Article sourcedFrom Self
            ),
            setOf(SpeechPart.Article)
        )
        val nounDefinitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            Self,
            CompulsoryData(false)
        )
        val nounDefinitenessExponenceCluster = ExponenceCluster(nounDefinitenessSourcedCategory)
        // Set up noun class
        val nounClassCategory = NounClass(
            listOf(NounClassValue.LongObject, NounClassValue.Fruit),
            setOf(
                SpeechPart.Noun sourcedFrom Self,
                SpeechPart.Article sourcedFrom Agreement(SyntaxRelation.Agent, nominals)
            ),
            setOf(SpeechPart.Noun)
        )
        val articleNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            Agreement(SyntaxRelation.Agent, nominals),
            CompulsoryData(true)
        )
        val articleNounClassExponenceCluster = ExponenceCluster(articleNounClassSourcedCategory)
        val nounNounClassSourcedCategory = SourcedCategory(
            nounClassCategory,
            Self,
            CompulsoryData(true)
        )
        val nounNounClassExponenceCluster = ExponenceCluster(nounNounClassSourcedCategory)
        // Set up WordChangeParadigm
        val nounDefinitenessApplicators = listOf(SuffixWordCategoryApplicator(article, LatchType.ClauseLatch))
        val nounClassApplicatiors = listOf(PassingCategoryApplicator, PassingCategoryApplicator)
        val articleApplicators = listOf(createAffixCategoryApplicator("b-"), createAffixCategoryApplicator("p-"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(nounDefinitenessExponenceCluster, nounNounClassExponenceCluster),
            mapOf(
                nounDefinitenessExponenceCluster to ValueMap(nounDefinitenessExponenceCluster.possibleValues, nounDefinitenessApplicators),
                nounNounClassExponenceCluster to ValueMap(nounNounClassExponenceCluster.possibleValues, nounClassApplicatiors)
            )
        )
        val articleSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Article.toDefault(),
            listOf(articleNounClassExponenceCluster),
            mapOf(articleNounClassExponenceCluster to ValueMap(articleNounClassExponenceCluster.possibleValues, articleApplicators))
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(nounClassCategory, definitenessCategory),
            mapOf(
                SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm,
                SpeechPart.Article.toDefault() to articleSpeechPartChangeParadigm
            )
        )

        assertEquals(
            listOf(
                // Noun form
                noun.withMorphemes(
                    MorphemeData(4, listOf(nounNounClassSourcedCategory[NounClassValue.Fruit]), true),
                ),
                // Article forms
                createWord("pa", SpeechPart.Article).withMorphemes(
                    MorphemeData(1, listOf(articleNounClassSourcedCategory[NounClassValue.Fruit])),
                    MorphemeData(1, listOf(), true)
                ).withStaticCategories(DefinitenessValue.Definite),
                createWord("ba", SpeechPart.Article).withMorphemes(
                    MorphemeData(1, listOf(articleNounClassSourcedCategory[NounClassValue.LongObject])),
                    MorphemeData(1, listOf(), true)
                ).withStaticCategories(DefinitenessValue.Definite)
            ).sortedBy { it.toString() },
            wordChangeParadigm.getUniqueWordForms(lexis)
                .distinct()
                .sortedBy { it.toString() }
        )
    }

    @Test
    fun `getUniqueWordForms works correctly with multiple exponence clusters`() {
        // Set up words
        val noun = createNoun("daba")
        val lexis = Lexis(listOf(noun), mapOf(), mapOf())
            .reifyPointers()
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite, DefinitenessValue.Indefinite),
            setOf(
                SpeechPart.Noun sourcedFrom Self
            )
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            Self,
            CompulsoryData(true)
        )
        val nounDefinitenessExponenceCluster = ExponenceCluster(definitenessSourcedCategory)
        // Set up number
        val numberCategory = Number(
            listOf(NumberValue.Singular, NumberValue.Plural),
            setOf(
                SpeechPart.Noun sourcedFrom Self,
            )
        )
        val numberSourcedCategory = SourcedCategory(
            numberCategory,
            Self,
            CompulsoryData(true)
        )
        val nounNumberExponenceCluster = ExponenceCluster(numberSourcedCategory)
        // Set up WordChangeParadigm
        val definitenessApplicators = listOf(
            createAffixCategoryApplicator("o-"),
            createAffixCategoryApplicator("a-")
        )
        val numberApplicatiors = listOf(PassingCategoryApplicator, createAffixCategoryApplicator("-i"))
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(nounDefinitenessExponenceCluster, nounNumberExponenceCluster),
            mapOf(
                nounDefinitenessExponenceCluster to ValueMap(nounDefinitenessExponenceCluster.possibleValues, definitenessApplicators),
                nounNumberExponenceCluster to ValueMap(nounNumberExponenceCluster.possibleValues, numberApplicatiors)
            )
        )
        val wordChangeParadigm = WordChangeParadigm(
            listOf(numberCategory, definitenessCategory),
            mapOf(
                SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm
            )
        )

        assertEquals(
            listOf(
                createNoun("odaba").withMorphemes(
                    MorphemeData(1, listOf(definitenessSourcedCategory[DefinitenessValue.Definite])),
                    MorphemeData(4, listOf(numberSourcedCategory[NumberValue.Singular]), true),
                ),
                createNoun("odabai").withMorphemes(
                    MorphemeData(1, listOf(definitenessSourcedCategory[DefinitenessValue.Definite])),
                    MorphemeData(4, listOf(), true),
                    MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural])),
                ),
                createNoun("adaba").withMorphemes(
                    MorphemeData(1, listOf(definitenessSourcedCategory[DefinitenessValue.Indefinite])),
                    MorphemeData(4, listOf(numberSourcedCategory[NumberValue.Singular]), true),
                ),
                createNoun("adabai").withMorphemes(
                    MorphemeData(1, listOf(definitenessSourcedCategory[DefinitenessValue.Indefinite])),
                    MorphemeData(4, listOf(), true),
                    MorphemeData(1, listOf(numberSourcedCategory[NumberValue.Plural])),
                )
            ).sortedBy { it.toString() },
            wordChangeParadigm.getUniqueWordForms(lexis)
                .distinct()
                .sortedBy { it.toString() }
        )
    }
}
