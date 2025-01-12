package io.tashtabash.lang.language.analyzer

import io.tashtabash.lang.language.category.NumberValue
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class WordFormAnalyzerTest {
    @Test
    fun `getIdenticalWordForms finds homophones`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("aba").withMeaning("crow"),
            createNoun("ubo"),
            createNoun("bacab"),
        )
        val derivations = listOf(
            Derivation(
                createAffix("-ob"),
                DerivationClass.AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger
            )
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("a-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        ).copy(prosodyChangeParadigm = ProsodyChangeParadigm(StressType.None))
        val language = makeDefLang(words, derivations, nounChangeParadigm)

        val wordForms = getIdenticalWordForms(language)

        assertEquals(
            listOf(
                listOf(
                    WordSequence(createNoun("aba")) to listOf<SourcedCategoryValues>(),
                    WordSequence(createNoun("aba").withMeaning("crow")) to listOf<SourcedCategoryValues>()
                )
            ),
            wordForms
        )
    }

    @Test
    fun `getIdenticalWordForms finds identical oblique forms`() {
        val words = listOf(
            createNoun("aba").withMeaning("crow"),
            createNoun("uba").withMeaning("dove"),
            createNoun("ubo"),
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("_- -> o-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix),
            isCompulsory = true
        ).copy(prosodyChangeParadigm = ProsodyChangeParadigm(StressType.None))
        val language = makeDefLang(words, listOf(), nounChangeParadigm)

        val wordForms = getIdenticalWordForms(language)

        val sgCategoryValue = nounChangeParadigm.getCategory("Number")
            .actualSourcedValues
            .find { it.categoryValue == NumberValue.Singular }
            .let { listOf(it!!) }
        val homophoneBase = createNoun("oba")
            .withMorphemes(
                MorphemeData(1, sgCategoryValue, false),
                MorphemeData(2, listOf(), true)
            )
        assertEquals(
            listOf(
                listOf(
                    WordSequence(homophoneBase.withMeaning("crow")) to sgCategoryValue,
                    WordSequence(homophoneBase.withMeaning("dove")) to sgCategoryValue
                )
            ),
            wordForms
        )
    }
}
