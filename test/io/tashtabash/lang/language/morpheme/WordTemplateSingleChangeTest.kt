package io.tashtabash.lang.language.morpheme

import io.tashtabash.lang.language.util.createNoun
import io.tashtabash.lang.language.util.getPhonySyllableTemplate
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.util.makePhoneme
import io.tashtabash.lang.language.util.makeSemanticsCore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.morphem.change.TemplateSingleChange
import io.tashtabash.lang.language.morphem.change.substitution.PassingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.ExactPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.phonology.matcher.ExactPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.TypePhonemeMatcher


internal class WordTemplateSingleChangeTest {
    @Test
    fun prefixIsolatingTest() {
        val prefix = listOf(
            makePhoneme("b", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )
        val affix = prefix.map { ExactPhonemeSubstitution(it) }
        val condition = listOf<PhonemeMatcher>()
        val substitution = listOf<PhonemeSubstitution>()
        val changeTemplate = TemplateSingleChange(
            Position.Beginning,
            condition,
            substitution,
            affix
        )
        val syllableTemplate = getPhonySyllableTemplate()
        val firstWord = createNoun(
            makePhoneme("b", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )
        val secondWord = createNoun(
            makePhoneme("c", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )

        checkForWord(
            firstWord,
            Word(
                listOf(
                    Syllable(prefix, 1)
                ) + firstWord.syllables,
                syllableTemplate,
                makeSemanticsCore()
            ),
            changeTemplate
        )
        checkForWord(
            secondWord,
            Word(
                listOf(
                    Syllable(prefix, 1)
                ) + secondWord.syllables,
                syllableTemplate,
                makeSemanticsCore()
            ),
            changeTemplate
        )
    }

    @Test
    fun prefixPhonemeTypeIsolatingTest() {
        val prefix = listOf(
            makePhoneme("b", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )
        val affix = prefix.map { ExactPhonemeSubstitution(it) }
        val substitution = listOf(PassingPhonemeSubstitution)
        val condition = listOf(
            TypePhonemeMatcher(PhonemeType.Consonant)
        )
        val changeTemplate = TemplateSingleChange(
            Position.Beginning,
            condition,
            substitution,
            affix
        )
        val syllableTemplate = getPhonySyllableTemplate()
        val correctWord = createNoun(
            makePhoneme("b", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )
        val badWord = createNoun(
            makePhoneme("a", PhonemeType.Vowel),
            makePhoneme("c", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )

        checkForWord(
            correctWord,
            Word(
                listOf(
                    Syllable(prefix, 1)
                ) + correctWord.syllables,
                syllableTemplate,
                makeSemanticsCore()
            ),
            changeTemplate
        )
        checkForWord(
            badWord,
            Word(
                badWord.syllables,
                syllableTemplate,
                makeSemanticsCore()
            ),
            changeTemplate
        )
    }

    @Test
    fun prefixStaticPhonemeIsolatingTest() {
        val prefix = listOf(
            makePhoneme("b", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )
        val affix = prefix.map { ExactPhonemeSubstitution(it) }
        val substitution = listOf(PassingPhonemeSubstitution)
        val condition = listOf(
            ExactPhonemeMatcher(makePhoneme("b", PhonemeType.Consonant))
        )
        val changeTemplate = TemplateSingleChange(
            Position.Beginning,
            condition,
            substitution,
            affix
        )
        val syllableTemplate = getPhonySyllableTemplate()
        val correctWord = createNoun(
            makePhoneme("b", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )
        val badWord = createNoun(
            makePhoneme("c", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
        )

        checkForWord(
            correctWord,
            Word(
                listOf(
                    Syllable(prefix, 1)
                ) + correctWord.syllables,
                syllableTemplate,
                makeSemanticsCore()
            ),
            changeTemplate
        )
        checkForWord(
            badWord,
            Word(
                badWord.syllables,
                syllableTemplate,
                makeSemanticsCore()
            ),
            changeTemplate
        )
    }

    private fun checkForWord(word: Word, result: Word, change: TemplateSingleChange) {
        assertEquals(result.toPhonemes(), change.change(word, listOf(), listOf()).toPhonemes())
    }
}
