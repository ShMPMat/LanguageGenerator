package io.tashtabash.lang.language.diachronicity.tendency

import io.tashtabash.lang.generator.ValueMap
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.Number
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.realization.PrefixWordCategoryApplicator
import io.tashtabash.lang.language.category.realization.SuffixWordCategoryApplicator
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.nominals
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ProgressiveNasalizationTest {
    @Test
    fun `computeDevelopmentChance() is 0 when a lang has no nasals before vowels`() {
        val language = makeDefLang(
            listOf(createNoun("ao"), createNoun("tado"), createNoun("adon"))
        )

        assertEquals(
            0.0,
            ProgressiveNasalization().computeDevelopmentChance(language)
        )
    }

    @Test
    fun `computeDevelopmentChance() is larger than 0 when a lang has nasals before vowels`() {
        val language = makeDefLang(
            listOf(createNoun("ao"), createNoun("tado"), createNoun("adono"))
        )

        assertTrue {
            ProgressiveNasalization().computeDevelopmentChance(language) > 0.0
        }
    }

    @Test
    fun `computeDevelopmentChance() is larger than 0 when a lang has nasals before vowels in oblique forms`() {
        val language = makeDefLang(
            listOf(createNoun("ao"), createNoun("tado"), createNoun("adon")),
            nounChangeParadigm = makeDefNounChangeParadigm(
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                createAffixCategoryApplicator("-o"),
            )
        )

        assertTrue {
            ProgressiveNasalization().computeDevelopmentChance(language) > 0.0
        }
    }

    @Test
    fun `computeDevelopmentChance() is larger than 0 when a lang has nasals before vowels in particles`() {
        val language = makeDefLang(
            listOf(createNoun("ao"), createNoun("tado"), createNoun("ado")),
            nounChangeParadigm = makeDefNounChangeParadigm(
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                PassingCategoryApplicator,
                PrefixWordCategoryApplicator(createWord("no", SpeechPart.Particle), LatchType.ClauseLatch),
            )
        )

        assertTrue {
            ProgressiveNasalization().computeDevelopmentChance(language) > 0.0
        }
    }

    @Test
    fun `computeDevelopmentChance() is larger than 0 when a lang has nasals before vowels in oblique function word forms`() {
        val article = createWord("an", SpeechPart.Article)
        // Set up definiteness
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(
                SpeechPart.Noun sourcedFrom CategorySource.Self,
                SpeechPart.Article sourcedFrom CategorySource.Self
            ),
            setOf(SpeechPart.Article)
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            CategorySource.Self,
            CompulsoryData(false)
        )
        val definitenessExponenceCluster = ExponenceCluster(definitenessSourcedCategory)
        // Set up number
        val numberCategory = Number(
            listOf(NumberValue.Singular, NumberValue.Plural),
            setOf(
                SpeechPart.Noun sourcedFrom CategorySource.Self,
                SpeechPart.Article sourcedFrom CategorySource.Agreement(SyntaxRelation.Agent, nominals)
            )
        )
        val articleNumberSourcedCategory = SourcedCategory(
            numberCategory,
            CategorySource.Agreement(SyntaxRelation.Agent, nominals),
            CompulsoryData(true)
        )
        val nounNumberSourcedCategory = SourcedCategory(
            numberCategory,
            CategorySource.Self,
            CompulsoryData(true)
        )
        val articleNumberExponenceCluster = ExponenceCluster(articleNumberSourcedCategory)
        val nounNumberExponenceCluster = ExponenceCluster(nounNumberSourcedCategory)
        // Set up SpeechPartChangeParadigms
        val nounDefinitenessApplicators = listOf(SuffixWordCategoryApplicator(article, LatchType.ClauseLatch))
        val nounNumberApplicatiors = listOf(PassingCategoryApplicator, PassingCategoryApplicator)
        val nounSpeechPartChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Noun.toDefault(),
            listOf(definitenessExponenceCluster, nounNumberExponenceCluster),
            mapOf(
                definitenessExponenceCluster to ValueMap(definitenessExponenceCluster.possibleValues, nounDefinitenessApplicators),
                nounNumberExponenceCluster to ValueMap(nounNumberExponenceCluster.possibleValues, nounNumberApplicatiors)
            )
        )
        val articleChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Article.toDefault(),
            listOf(articleNumberExponenceCluster),
            mapOf(articleNumberExponenceCluster to ValueMap(
                articleNumberExponenceCluster.possibleValues,
                listOf(PassingCategoryApplicator, createAffixCategoryApplicator("-o"))
            )),
            ProsodyChangeParadigm(StressType.Initial)
        )
        // Set up language
        val language = makeDefLang(
            listOf(createNoun("ao"), createNoun("tado"), createNoun("ado"), article),
            WordChangeParadigm(
                listOf(definitenessCategory, numberCategory),
                mapOf(
                    SpeechPart.Noun.toDefault() to nounSpeechPartChangeParadigm,
                    SpeechPart.Article.toDefault() to articleChangeParadigm
                )
            )
        )

        assertTrue {
            ProgressiveNasalization().computeDevelopmentChance(language) > 0.0
        }
    }
}
