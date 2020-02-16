package shmp.language.morphem

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import shmp.generator.SyllableTemplate
import shmp.generator.SyllableValenceTemplate
import shmp.generator.ValencyPlace
import shmp.language.*

internal class WordTemplateChangeTest {
    @Test
    fun changePrefix() {
        val prefix = listOf(
            Phoneme("b", PhonemeType.Consonant),
            Phoneme("a", PhonemeType.Vowel)
        )
        val substitution = prefix.map { PhonemePositionSubstitution(it) } +
                PassingPositionSubstitution()
        val condition = listOf(
            PhonemeTemplate(Phoneme("b", PhonemeType.Consonant))
        )
        val changeTemplate = TemplateChange(Position.Beginning, condition, substitution)
        val syllableTemplate = getPhonySyllableTemplate()
        val applicableWord = Word(
            listOf(Syllable(listOf(Phoneme("b", PhonemeType.Consonant), Phoneme("a", PhonemeType.Vowel)))),
            syllableTemplate,
            SyntaxCore("phony", SpeechPart.Noun)
        )

        checkForWord(
            applicableWord,
            Word(
                listOf(
                    Syllable(prefix)
                ) + applicableWord.syllables,
                syllableTemplate,
                SyntaxCore("phony", SpeechPart.Noun)
            ),
            changeTemplate
        )

    }

    private fun checkForWord(word: Word, result: Word, change: TemplateChange) {
        assertEquals(result.toPhonemes(), change.change(word).toPhonemes())
    }

    private fun getPhonySyllableTemplate(): SyllableTemplate =
        SyllableValenceTemplate(
            listOf(
                ValencyPlace(PhonemeType.Consonant, 1.0),
                ValencyPlace(PhonemeType.Vowel, 1.0)
            )
        )
}