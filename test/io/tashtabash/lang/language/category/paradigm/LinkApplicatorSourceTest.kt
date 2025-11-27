package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.Tense
import io.tashtabash.lang.language.category.TenseValue
import io.tashtabash.lang.language.category.sourcedFrom
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.util.createAffixCategoryApplicator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class LinkApplicatorSourceTest {
    @Test
    fun `Correctly resolves the applicator map`() {
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

        val intransVerbLink = LinkApplicatorSource(verbSpeechPartChangeParadigm, 0)

        assertEquals(
            verbSpeechPartChangeParadigm.applicators[0]
                .second
                .map,
            intransVerbLink.map
        )
    }
}
