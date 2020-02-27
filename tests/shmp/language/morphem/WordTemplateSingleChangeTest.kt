package shmp.language.morphem

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException
import shmp.language.phonology.SyllableTemplate
import shmp.language.phonology.SyllableValenceTemplate
import shmp.language.phonology.ValencyPlace
import shmp.language.*
import shmp.language.morphem.change.*
import shmp.language.phonology.Phoneme
import shmp.language.phonology.PhonemeSequence
import shmp.language.phonology.Syllable

internal class WordTemplateSingleChangeTest {
    @Test
    fun prefixIsolatingTest() {
        val prefix = listOf(
            Phoneme("b", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )
        val affix = prefix.map { PhonemePositionSubstitution(it) }
        val condition = listOf<PhonemeMatcher>()
        val substitution = listOf<PositionSubstitution>()
        val changeTemplate = TemplateSingleChange(
            Position.Beginning,
            condition,
            substitution,
            affix
        )
        val syllableTemplate = getPhonySyllableTemplate()
        val firstWord = createNoun(
            Phoneme("b", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )
        val secondWord = createNoun(
            Phoneme("c", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )

        checkForWord(
            firstWord,
            Word(
                listOf(
                    Syllable(prefix)
                ) + firstWord.syllables,
                syllableTemplate,
                SyntaxCore("phony", SpeechPart.Noun)
            ),
            changeTemplate
        )
        checkForWord(
            secondWord,
            Word(
                listOf(
                    Syllable(prefix)
                ) + secondWord.syllables,
                syllableTemplate,
                SyntaxCore("phony", SpeechPart.Noun)
            ),
            changeTemplate
        )
    }

    @Test
    fun prefixPhonemeTypeIsolatingTest() {
        val prefix = listOf(
            Phoneme("b", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )
        val affix = prefix.map { PhonemePositionSubstitution(it) }
        val substitution = listOf(PassingPositionSubstitution())
        val condition = listOf(
            TypePositionMatcher(PhonemeType.Consonant)
        )
        val changeTemplate = TemplateSingleChange(
            Position.Beginning,
            condition,
            substitution,
            affix
        )
        val syllableTemplate = getPhonySyllableTemplate()
        val correctWord = createNoun(
            Phoneme("b", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )
        val badWord = createNoun(
            Phoneme("a", PhonemeType.Vowel),
            Phoneme("c", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )

        checkForWord(
            correctWord,
            Word(
                listOf(
                    Syllable(prefix)
                ) + correctWord.syllables,
                syllableTemplate,
                SyntaxCore("phony", SpeechPart.Noun)
            ),
            changeTemplate
        )
        checkForWord(
            badWord,
            Word(
                badWord.syllables,
                syllableTemplate,
                SyntaxCore("phony", SpeechPart.Noun)
            ),
            changeTemplate
        )
    }

    @Test
    fun prefixStaticPhonemeIsolatingTest() {
        val prefix = listOf(
            Phoneme("b", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )
        val affix = prefix.map { PhonemePositionSubstitution(it) }
        val substitution = listOf(PassingPositionSubstitution())
        val condition = listOf(
            PhonemeMatcher(Phoneme("b", PhonemeType.Consonant))
        )
        val changeTemplate = TemplateSingleChange(
            Position.Beginning,
            condition,
            substitution,
            affix
        )
        val syllableTemplate = getPhonySyllableTemplate()
        val correctWord = createNoun(
            Phoneme("b", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )
        val badWord = createNoun(
            Phoneme("c", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )

        checkForWord(
            correctWord,
            Word(
                listOf(
                    Syllable(prefix)
                ) + correctWord.syllables,
                syllableTemplate,
                SyntaxCore("phony", SpeechPart.Noun)
            ),
            changeTemplate
        )
        checkForWord(
            badWord,
            Word(
                badWord.syllables,
                syllableTemplate,
                SyntaxCore("phony", SpeechPart.Noun)
            ),
            changeTemplate
        )
    }

    private fun checkForWord(word: Word, result: Word, change: TemplateSingleChange) {
        assertEquals(result.toPhonemes(), change.change(word).toPhonemes())
    }

    private fun createNoun(vararg phonemes: Phoneme) = getPhonySyllableTemplate().createWord(
        PhonemeSequence(phonemes.toList()),
        SyntaxCore("phony", SpeechPart.Noun)
    ) ?: throw TestAbortedException("Wrong word creation")

    private fun getPhonySyllableTemplate(): SyllableTemplate =
        SyllableValenceTemplate(
            listOf(
                ValencyPlace(PhonemeType.Consonant, 0.5),
                ValencyPlace(PhonemeType.Vowel, 1.0)
            )
        )
}