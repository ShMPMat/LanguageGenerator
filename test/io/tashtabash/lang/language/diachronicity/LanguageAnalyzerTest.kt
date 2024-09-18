package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.util.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class LanguageAnalyzerTest {
    @Test
    fun `analyzePhonemes returns phonemes present in a language`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(
                createAffix("-ab"),
                DerivationClass.AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger
            ),
            Derivation(
                createAffix("ac-"),
                DerivationClass.AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger
            ),
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
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)

        val phonemeContainer = analyzePhonemes(language)

        assertEquals(createPhonemes("abouc"), phonemeContainer.phonemes)
    }
}