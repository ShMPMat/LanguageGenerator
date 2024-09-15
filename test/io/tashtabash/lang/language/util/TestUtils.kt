package io.tashtabash.lang.language.util

import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.phonology.*
import org.opentest4j.TestAbortedException



fun createNoun(vararg phonemes: Phoneme) =
    createNoun(phonemes.toList())

fun createNoun(phonemes: List<Phoneme>) = getPhonySyllableTemplate().createWord(
    PhonemeSequence(phonemes.toList()),
    makeSemanticsCore()
) ?: throw TestAbortedException("Wrong word creation")

fun Word.withMorphemes(rootIdx: Int, vararg lengths: Int) =
    copy(
        morphemes = lengths.mapIndexed { i, it -> MorphemeData(it, listOf(), i == rootIdx) }
    )

fun getPhonySyllableTemplate(): SyllableTemplate =
    SyllableValenceTemplate(
        listOf(
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Vowel, 1.0),
            ValencyPlace(PhonemeType.Consonant, 0.5),
            ValencyPlace(PhonemeType.Consonant, 0.5)
        )
    )

fun makePhoneme(name: String, type: PhonemeType) =
    Phoneme(name, type, ArticulationPlace.Bilabial, ArticulationManner.Close, setOf())

fun makeSemanticsCore() =
    SemanticsCore(MeaningCluster("phony"), TypedSpeechPart(SpeechPart.Noun), 1.0)
