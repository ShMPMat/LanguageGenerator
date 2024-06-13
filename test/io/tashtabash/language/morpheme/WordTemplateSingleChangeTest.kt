package io.tashtabash.language.morpheme

import io.tashtabash.lang.language.lexis.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException
import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.morphem.change.TemplateSingleChange
import io.tashtabash.lang.language.morphem.change.matcher.PhonemeMatcher
import io.tashtabash.lang.language.morphem.change.matcher.TypePositionMatcher
import io.tashtabash.lang.language.morphem.change.substitution.PassingPositionSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemePositionSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PositionSubstitution
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.language.*


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
            TypePositionMatcher(PhonemeType.Consonant, true)
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
            PhonemeMatcher(makePhoneme("b", PhonemeType.Consonant), true)
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
        Phoneme(name, type, ArticulationPlace.Bilabial, ArticulationManner.Close, setOf())

    private fun makeSemanticsCore() =
        SemanticsCore(MeaningCluster("phony"), TypedSpeechPart(SpeechPart.Noun), 1.0)
}
