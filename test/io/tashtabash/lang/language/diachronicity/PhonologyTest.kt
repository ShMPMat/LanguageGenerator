package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationClass.AbstractNounFromNoun
import io.tashtabash.lang.language.printWordMorphemes
import io.tashtabash.lang.language.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals


internal class PhonologyTest {
    @Test
    fun `applyPhonologicalRule changes a single vowel`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("a-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> i / _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("ibi"),
                createNoun("ibo"),
                createNoun("ubo"),
                createNoun("bicib"),
                createNoun("bob"),
                createNoun("bic")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-ib"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ic-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("i-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule changes a single consonant`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("a-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("b -> t / _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("ata"),
                createNoun("ato"),
                createNoun("uto"),
                createNoun("tacat"),
                createNoun("tot"),
                createNoun("tac")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-ot"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("a-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("t-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-ot"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule changes the start vowel`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("a-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> i / $ _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("iba"),
                createNoun("ibo"),
                createNoun("ubo"),
                createNoun("bacab"),
                createNoun("bob"),
                createNoun("bac")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ic-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("i-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule changes the end consonant`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("a-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("b -> t / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("aba"),
                createNoun("abo"),
                createNoun("ubo"),
                createNoun("bacat"),
                createNoun("bot"),
                createNoun("bac")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-ot"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("a-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-ot"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule deletes the last exact vowel`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("-ba"), CategoryRealization.Suffix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> - / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("ab"),
                createNoun("abo"),
                createNoun("ubo"),
                createNoun("bacab"),
                createNoun("bob"),
                createNoun("bac")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("-b"), CategoryRealization.Suffix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
        assertDoesNotThrow {
            val wordForms = shiftedLanguage.lexis
                .words
                .flatMap { language.changeParadigm.wordChangeParadigm.getAllWordForms(it, true) }
            for ((wordSequence) in wordForms)
                printWordMorphemes(wordSequence.words[0])
        }
    }

    @Test
    fun `applyPhonologicalRule deletes the last consonant`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-b -> tab", "-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("c- -> abc", "ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("-ba"), CategoryRealization.Suffix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("C -> - / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("aba"),
                createNoun("abo"),
                createNoun("ubo"),
                createNoun("baca"),
                createNoun("bo"),
                createNoun("ba")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-b -> ta", "-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("c- -> abc", "ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-o"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("-ba"), CategoryRealization.Suffix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-o"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
        assertDoesNotThrow {
            val wordForms = shiftedLanguage.lexis
                .words
                .flatMap { language.changeParadigm.wordChangeParadigm.getAllWordForms(it, true) }
            for ((wordSequence) in wordForms)
                printWordMorphemes(wordSequence.words[0])
        }
    }

    @Test
    fun `applyPhonologicalRule changes a consonant before a vowel`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("ubo"),
            createNoun("bacab"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("C- -> b_"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)

        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("-ba"), CategoryRealization.Suffix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("b -> t / _ V")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("ata"),
                createNoun("ato"),
                createNoun("uto"),
                createNoun("tacab"),
                createNoun("tob"),
                createNoun("tac")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-b -> tab", "-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-b -> tob", "-ob"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-b -> ta", "-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("C- -> b_"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("-ta"), CategoryRealization.Suffix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("V- -> t_", "b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-b -> tob", "-ob"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule deletes a vowel between consonants`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("bob"),
            createNoun("baco"),
            createNoun("bacab")
        )
        val derivations = listOf(
            Derivation(createAffix("-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ca-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("C- -> b_"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("-ba"), CategoryRealization.Suffix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("V -> - / \$C _ CV")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("aba"),
                createNoun("abo"),
                createNoun("bob"),
                createNoun("bco"),
                createNoun("bcab")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-\$CVC -> __-_ab", "-ab"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("CV- -> c__", "ca-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-\$CVC -> __-_a", "-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("C- -> b_"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("-\$CV -> __-ba", "-ba"), CategoryRealization.Suffix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("VCV- -> b-__", "b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-\$CVC -> __-_ob", "-ob"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule saves messages if a word wasn't changed`() {
        val words = listOf(
            createNoun("bab"),
        )
        val derivations = listOf(
            Derivation(createAffix("-a"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            AffixCategoryApplicator(createAffix("-ba"), CategoryRealization.Suffix),
            AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
            AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> - / _ ")

        val phonologicalRuleApplicator = PhonologicalRuleApplicator()
        val shiftedLanguage = phonologicalRuleApplicator.applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("bab"),
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                AffixCategoryApplicator(createAffix("-b"), CategoryRealization.Suffix),
                AffixCategoryApplicator(createAffix("u-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("b-"), CategoryRealization.Prefix),
                AffixCategoryApplicator(createAffix("-ob"), CategoryRealization.Suffix)
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
        assertEquals(
            listOf("Can't split the word 'bab' on syllables after applying changes, reverting the word"),
            phonologicalRuleApplicator.messages
        )
    }
}