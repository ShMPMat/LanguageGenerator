package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.CompulsoryData
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.LatchedWord
import io.tashtabash.lang.language.util.createNoun
import io.tashtabash.lang.language.util.withMorphemes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class PassingCategoryApplicatorTest {
    @Test
    fun `Injects the values into a root`() {
        // Set up words
        val word = createNoun("a")
        val wordSequence = FoldedWordSequence(LatchedWord(word, LatchType.Center))
        // Set up a category
        val definitenessCategory = Definiteness(
            listOf(DefinitenessValue.Definite),
            setOf(
                SpeechPart.Noun sourcedFrom CategorySource.Self
            ),
            setOf(SpeechPart.Article)
        )
        val definitenessSourcedCategory = SourcedCategory(
            definitenessCategory,
            CategorySource.Self,
            CompulsoryData(false)
        )
        val definiteValue = definitenessSourcedCategory[DefinitenessValue.Definite]

        assertEquals(
            PassingCategoryApplicator.apply(wordSequence, 0, listOf(definiteValue)),
            wordSequence.map { it.withMorphemes(MorphemeData(1, listOf(definiteValue), true)) }
        )
    }
}
