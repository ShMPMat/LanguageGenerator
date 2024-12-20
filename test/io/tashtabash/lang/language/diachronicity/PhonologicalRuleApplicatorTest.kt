package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.DerivationClusterTemplate
import io.tashtabash.lang.containers.SemanticsCoreTemplate
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.NumeralSystemBase
import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.realization.SuppletionCategoryApplicator
import io.tashtabash.lang.language.derivation.*
import io.tashtabash.lang.language.derivation.DerivationClass.AbstractNounFromNoun
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.phonology.prosody.Prosody
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.printWordMorphemes
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.lang.language.syntax.numeral.NumeralParadigm
import io.tashtabash.lang.language.util.*
import io.tashtabash.random.singleton.RandomSingleton
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.random.Random
import kotlin.test.assertContains
import kotlin.test.assertEquals


internal class PhonologicalRuleApplicatorTest {
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
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            SuppletionCategoryApplicator(createNoun("baboboba")),
            createAffixCategoryApplicator("-ob")
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
                createAffixCategoryApplicator("i-"),
                createAffixCategoryApplicator("u-"),
                SuppletionCategoryApplicator(createNoun("bibobobi")),
                createAffixCategoryApplicator("-ob")
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
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
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
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("t-"),
                createAffixCategoryApplicator("-ot")
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
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> i / $ _ ")

        val shiftedLanguage = PhonologicalRuleApplicator(true).applyPhonologicalRule(language, phonologicalRule)

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
                createAffixCategoryApplicator("i-"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("-ob")
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
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("b -> t / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator(true).applyPhonologicalRule(language, phonologicalRule)

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
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("-ot")
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
            createAffixCategoryApplicator("-ba"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> - / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator(true).applyPhonologicalRule(language, phonologicalRule)

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
                createAffixCategoryApplicator("-b"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("-ob")
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
            createAffixCategoryApplicator("-ba"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("C -> - / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator(true).applyPhonologicalRule(language, phonologicalRule)

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
                createAffixCategoryApplicator("-ba"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("-o")
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
            createAffixCategoryApplicator("-ba"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
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
                createAffixCategoryApplicator("-ta"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("V- -> t_", "b-"),
                createAffixCategoryApplicator("-b -> tob", "-ob")
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
            createNoun("ba"),
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
            createAffixCategoryApplicator("-ba"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("V -> - / \$C _ CV")

        val shiftedLanguage = PhonologicalRuleApplicator(true).applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("aba"),
                createNoun("abo"),
                createNoun("bob"),
                createNoun("bco"),
                createNoun("ba"),
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
                createAffixCategoryApplicator("-\$CV -> __-ba", "-ba"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("VCV- -> b-__", "b-"),
                createAffixCategoryApplicator("-\$CVC -> __-_ob", "-ob")
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
            createAffixCategoryApplicator("-ba"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
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
                createAffixCategoryApplicator("-b"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("-ob")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
        assertEquals(
            listOf("Can't split the word 'bab' on syllables after applying the rule, reverting the word"),
            phonologicalRuleApplicator.messages
        )
    }

    @Test
    fun `applyPhonologicalRule makes a consonant voiced`() {
        val words = listOf(
            createNoun("aba"),
            createNoun("abo"),
            createNoun("apa"),
            createNoun("ata"),
            createNoun("apo"),
            createNoun("ub"),
            createNoun("ut"),
            createNoun("bob"),
            createNoun("bac")
        )
        val derivations = listOf(
            Derivation(createAffix("-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ta-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("to-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("da-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("t-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-od"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("t-"),
            createAffixCategoryApplicator("d-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("[-Voiced] -> [+Voiced] / _ a")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("aba"),
                createNoun("abo"),
                createNoun("aba"),
                createNoun("ada"),
                createNoun("apo"),
                createNoun("ub"),
                createNoun("ut"),
                createNoun("bob"),
                createNoun("bac")
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-[-Voiced] -> [+Voiced]at", "-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("da-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("to-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("da-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("a- -> d_", "t-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-od"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("a- -> d_", "t-"),
                createAffixCategoryApplicator("d-"),
                createAffixCategoryApplicator("-ob")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule matches prosody`() {
        val words = listOf(
            createNoun("aba").withProsodyOn(0, Prosody.Stress),
            createNoun("oba").withProsodyOn(1, Prosody.Stress),
            createNoun("apapapa").withProsodyOn(0, Prosody.Stress)
                .withProsodyOn(3, Prosody.Stress),
        )
        val derivations = listOf(
            Derivation(createAffix("-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ta-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("t-"),
            createAffixCategoryApplicator("d-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("(a{+Stress}) -> o / _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("oba").withProsodyOn(0, Prosody.Stress),
                createNoun("obo").withProsodyOn(1, Prosody.Stress),
                createNoun("opapapo").withProsodyOn(0, Prosody.Stress)
                    .withProsodyOn(3, Prosody.Stress),
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("ta-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("t-"),
                createAffixCategoryApplicator("d-"),
                createAffixCategoryApplicator("-ob")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule makes a deletes a consonant changing the syllable structure`() {
        val words = listOf(
            createNoun("tat"),
            createNoun("aba"),
            createNoun("bapo"),
            createNoun("bopo"),
            createNoun("pa"),
            createNoun("ata"),
        )
        val derivations = listOf(
            Derivation(createAffix("-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("ta-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("to-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("da-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("t-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            Derivation(createAffix("-od"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("t-"),
            createAffixCategoryApplicator("d-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> - / \$C _ CV")
            .copy(allowSyllableStructureChange = true)

        val phonologicalRuleApplicator = PhonologicalRuleApplicator(true)
        val shiftedLanguage = phonologicalRuleApplicator.applyPhonologicalRule(language, phonologicalRule)

        val expectedSyllableTemplate = SyllableValenceTemplate(
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Vowel, 1.0),
            ValencyPlace(PhonemeType.Consonant, 0.5)
        )
        assertEquals(phonologicalRuleApplicator.messages, listOf())
        assertEquals(
            listOf(
                createNoun("tat", expectedSyllableTemplate),
                createNoun("aba", expectedSyllableTemplate),
                createNoun("bpo", expectedSyllableTemplate),
                createNoun("bopo", expectedSyllableTemplate),
                createNoun("pa", expectedSyllableTemplate),
                createNoun("ata", expectedSyllableTemplate),
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-\$CaC -> __-_at", "-at"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("CV- -> t__", "ta-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("to-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("CV- -> d__", "da-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("a-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("aCV- -> t-__", "t-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
                Derivation(createAffix("-\$CaC -> __-_od", "-od"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("aCV- -> t-__", "t-"),
                createAffixCategoryApplicator("aCV- -> d-__", "d-"),
                createAffixCategoryApplicator("-\$CaC -> __-_ob", "-ob")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule saves prosody`() {
        val words = listOf(
            createNoun("abab")
                .withProsodyOn(0, Prosody.Stress),
        )
        val derivations = listOf<Derivation>()
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("p-")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("b -> - / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createNoun("aba")
                    .withProsodyOn(0, Prosody.Stress)
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("p-")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule saves prosody when syllable recalculation is applied`() {
        val words = listOf(
            createNoun("abab")
                .withProsodyOn(0, Prosody.Stress),
        )
        val derivations = listOf<Derivation>()
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("p-")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("b -> - / _ $!")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)
        val expectedSyllableTemplate = SyllableValenceTemplate(
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Vowel, 1.0),
        )

        assertEquals(
            listOf(
                createNoun("aba", expectedSyllableTemplate)
                    .withProsodyOn(0, Prosody.Stress)
            ),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("p-")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule doesn't break the DerivationHistory output`() {
        val derivation = Derivation(createAffix("ba-"), AbstractNounFromNoun, defSpeechPart, 100.0, defCategoryChanger)
        val stemWord = createNoun("babo").let {
            it.copy(
                semanticsCore = it.semanticsCore.copy(
                    derivationCluster = DerivationCluster(
                        mapOf(DerivationType.NNAbstract to listOf(DerivationLink("phony derived", 100.0)))
                    )
                )
            )
        }
        val derivedWord = derivation
            .deriveRandom(stemWord, Random(0)) { SemanticsCoreTemplate(it, SpeechPart.Noun) }!!
        val words = listOf(stemWord, derivedWord)
        val derivations = listOf(derivation)
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("p-")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> o / _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createPhonemes("bobo"),
                createPhonemes("bobobo"),
            ),
            shiftedLanguage.lexis.words.map { it.toPhonemes() }
        )
        assertEquals(
            "bobo  ->AbstractNounFromNoun-> bobobo       \n" +
                    "phony ->AbstractNounFromNoun-> phony derived",
            shiftedLanguage.lexis.computeHistory(shiftedLanguage.lexis.words[1])
        )
        assertEquals(
            Derivation(createAffix("bo-"), AbstractNounFromNoun, defSpeechPart, 100.0, defCategoryChanger),
            (shiftedLanguage.lexis.words[1].semanticsCore.changeHistory as DerivationHistory).derivation
        )
    }


    @Test
    fun `applyPhonologicalRule doesn't break the CompoundHistory output`() {
        RandomSingleton.safeRandom = Random(1)
        val lexis = Lexis(
            listOf(
                createNoun("babo").withMeaning("left"),
                createNoun("papo").withMeaning("right"),
            ),
            mapOf(),
            mapOf()
        )
        val compound = Compound(
            SpeechPart.Noun.toDefault(),
            PhonemeSequence(createPhonemes("ta")),
            ConstantCategoryChanger(setOf(), SpeechPart.Noun.toDefault()),
            PassingProsodyRule
        )
        val semanticsCoreTemplate = SemanticsCoreTemplate(
            "Compound",
            SpeechPart.Noun,
            derivationClusterTemplate = DerivationClusterTemplate(
                possibleCompounds = mutableListOf(CompoundLink(listOf("left", "right"), 100.0))
            )
        )
        val compoundWord = compound.compose(lexis, semanticsCoreTemplate, Random(1))!!
        val words = lexis.words + compoundWord
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("p-")
        )
        val language = makeDefLang(words, listOf(), nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> o / _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(
                createPhonemes("bobo"),
                createPhonemes("popo"),
                createPhonemes("bobotopopo"),
            ),
            shiftedLanguage.lexis.words.map { it.toPhonemes() }
        )
        assertEquals(
            "bobo  ->to-> \n" +
                    "left  ->to-> \n" +
                    "popo  ->to-> bobotopopo\n" +
                    "right ->to-> Compound  ",
            shiftedLanguage.lexis.computeHistory(shiftedLanguage.lexis.words[2])
        )
    }

    @Test
    fun `applyPhonologicalRule doesn't break the copula and question marker pointers`() {
        val copulaWord = createWord("ba", SpeechPart.Particle)
        val questionMarkerWord = createWord("pa", SpeechPart.Particle)
        val words = listOf(copulaWord, questionMarkerWord)
        val nounChangeParadigm = SpeechPartChangeParadigm(
            defSpeechPart,
            listOf(),
            mapOf(),
            ProsodyChangeParadigm(StressType.Initial)
        )
        val particleChangeParadigm = SpeechPartChangeParadigm(
            SpeechPart.Particle.toDefault(),
            listOf(),
            mapOf(),
            ProsodyChangeParadigm(StressType.Initial)
        )
        val lexis = Lexis(
            words,
            mapOf(CopulaType.Particle to SimpleWordPointer(copulaWord)),
            mapOf(QuestionMarker to SimpleWordPointer(questionMarkerWord))
        ).reifyPointers()
        val language = Language(
            lexis,
            testPhonemeContainer,
            StressType.Initial,
            RestrictionsParadigm(mutableMapOf()),
            DerivationParadigm(listOf(), listOf()),
            ChangeParadigm(
                WordOrder(mapOf(), mapOf(), NominalGroupOrder.DNP),
                WordChangeParadigm(
                    listOf(),
                    mapOf(defSpeechPart to nounChangeParadigm, SpeechPart.Particle.toDefault() to particleChangeParadigm)
                ),
                SyntaxParadigm(
                    CopulaPresence(listOf(CopulaType.None.toSso(1.0))),
                    QuestionMarkerPresence(null),
                    PredicatePossessionPresence(listOf(PredicatePossessionType.HaveVerb.toSso(1.0)))
                ),
                NumeralParadigm(NumeralSystemBase.Restricted3, listOf()),
                SyntaxLogic(mapOf(), mapOf(), mapOf(), mapOf(), null, mapOf(), mapOf(), listOf(), null)
            )
        )
        val phonologicalRule = createTestPhonologicalRule("a -> o / _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            createWord("bo", SpeechPart.Particle),
            shiftedLanguage.lexis.copula[CopulaType.Particle]?.resolve(shiftedLanguage.lexis)
        )
        assertEquals(
            createWord("po", SpeechPart.Particle),
            shiftedLanguage.lexis.questionMarker[QuestionMarker]?.resolve(shiftedLanguage.lexis)
        )
    }

    @Test
    fun `applyPhonologicalRule doesn't change a language if a rule isn't applicable`() {
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
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("t -> b / _ ")

        val phonologicalRuleApplicator = PhonologicalRuleApplicator()
        val shiftedLanguage = phonologicalRuleApplicator.applyPhonologicalRule(language, phonologicalRule)

        assertEquals(language, shiftedLanguage)
        assertContains(phonologicalRuleApplicator.messages, "Rule t -> b /  _  didn't have any effect on the language")
    }

    @Test
    fun `applyPhonologicalRule doesn't change an affix if its new form isn't used in any possible form`() {
        val words = listOf(createNoun("atab"))
        val derivations = listOf(
            Derivation(createAffix("-t"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            SuppletionCategoryApplicator(createNoun("at")),
            createAffixCategoryApplicator("-t")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("[-Voiced] -> [+Voiced] / V _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(createNoun("adab")),
            shiftedLanguage.lexis.words
        )
        // The derivations aren't cleared
        assertEquals(
            listOf(
                Derivation(createAffix("-V -> _d", "-t"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                SuppletionCategoryApplicator(createNoun("ad")),
                createAffixCategoryApplicator("-t")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule swaps Suffix for PassingCategoryApplicator if it is empty after applying a rule`() {
        val words = listOf(createNoun("atab"))
        val derivations = listOf(
            Derivation(createAffix("-ta"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            SuppletionCategoryApplicator(createNoun("at")),
            createAffixCategoryApplicator("-V -> _", "-t")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("t -> - / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator(true).applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(createNoun("atab")),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("-ta"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                SuppletionCategoryApplicator(createNoun("a")),
                PassingCategoryApplicator
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule swaps Prefix for PassingCategoryApplicator if it is empty after applying a rule`() {
        val words = listOf(createNoun("atab"))
        val derivations = listOf(
            Derivation(createAffix("t-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("-a"),
            createAffixCategoryApplicator("-a"),
            SuppletionCategoryApplicator(createNoun("at")),
            createAffixCategoryApplicator("C- -> _", "a-")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> - / $ _ ")

        val shiftedLanguage = PhonologicalRuleApplicator(true).applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(createNoun("tab")),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("t-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger),
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("-a"),
                createAffixCategoryApplicator("-a"),
                SuppletionCategoryApplicator(createNoun("at")),
                PassingCategoryApplicator
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
    }

    @Test
    fun `applyPhonologicalRule is added to sandhi if it matches the word start and the lang has prefixes`() {
        val words = listOf(createNoun("aca"))
        val derivations = listOf(
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> i / $ _ ")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(createNoun("aca")),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("-ob")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
        assertEquals(
            listOf(phonologicalRule),
            shiftedLanguage.changeParadigm.wordChangeParadigm.sandhiRules,
        )
    }

    @Test
    fun `applyPhonologicalRule is added to sandhi if it matches the word end and the lang has suffixes`() {
        val words = listOf(createNoun("aca"))
        val derivations = listOf(
            Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
        )
        val nounChangeParadigm = makeDefNounChangeParadigm(
            createAffixCategoryApplicator("a-"),
            createAffixCategoryApplicator("u-"),
            createAffixCategoryApplicator("b-"),
            createAffixCategoryApplicator("-ob")
        )
        val language = makeDefLang(words, derivations, nounChangeParadigm)
        val phonologicalRule = createTestPhonologicalRule("a -> i / _ $")

        val shiftedLanguage = PhonologicalRuleApplicator().applyPhonologicalRule(language, phonologicalRule)

        assertEquals(
            listOf(createNoun("aca")),
            shiftedLanguage.lexis.words
        )
        assertEquals(
            listOf(
                Derivation(createAffix("ac-"), AbstractNounFromNoun, defSpeechPart, 1.0, defCategoryChanger)
            ),
            shiftedLanguage.derivationParadigm.derivations
        )
        assertEquals(
            makeDefNounChangeParadigm(
                createAffixCategoryApplicator("a-"),
                createAffixCategoryApplicator("u-"),
                createAffixCategoryApplicator("b-"),
                createAffixCategoryApplicator("-ob")
            ),
            shiftedLanguage.changeParadigm.wordChangeParadigm.speechPartChangeParadigms[defSpeechPart],
        )
        assertEquals(
            listOf(phonologicalRule),
            shiftedLanguage.changeParadigm.wordChangeParadigm.sandhiRules,
        )
    }
}
