package shmp.language.morphem

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException
import shmp.language.*
import shmp.language.lexis.MeaningCluster
import shmp.language.lexis.SemanticsCore
import shmp.language.lexis.Word
import shmp.language.morphem.change.*
import shmp.language.phonology.*


internal class WordTemplateSingleChangeTest {
    @Test
    fun prefixIsolatingTest() {
        val prefix = listOf(
            makePhoneme("b", PhonemeType.Consonant),
            makePhoneme("a", PhonemeType.Vowel)
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
                    Syllable(prefix)
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
                    Syllable(prefix)
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
                    Syllable(prefix)
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
        val affix = prefix.map { PhonemePositionSubstitution(it) }
        val substitution = listOf(PassingPositionSubstitution())
        val condition = listOf(
            PhonemeMatcher(makePhoneme("b", PhonemeType.Consonant))
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
                    Syllable(prefix)
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
        assertEquals(result.toPhonemes(), change.change(word).toPhonemes())
    }

    private fun createNoun(vararg phonemes: Phoneme) = getPhonySyllableTemplate().createWord(
        PhonemeSequence(phonemes.toList()),
        makeSemanticsCore()
    ) ?: throw TestAbortedException("Wrong word creation")

    private fun getPhonySyllableTemplate(): SyllableTemplate =
        SyllableValenceTemplate(
            listOf(
                ValencyPlace(PhonemeType.Consonant, 0.5),
                ValencyPlace(PhonemeType.Vowel, 1.0)
            )
        )

    private fun makePhoneme(name: String, type: PhonemeType) =
        Phoneme(name, type, ArticulationPlace.Bilabial, ArticulationManner.Close)

    private fun makeSemanticsCore() =
        SemanticsCore(MeaningCluster("phony"), SpeechPart.Noun, setOf())
}
