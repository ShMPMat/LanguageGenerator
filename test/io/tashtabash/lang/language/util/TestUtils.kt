package io.tashtabash.lang.language.util

import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.phonology.*
import org.opentest4j.TestAbortedException


fun createWord(
    phonemes: List<Phoneme>,
    speechPart: SpeechPart,
    syllableTemplate: SyllableTemplate = getPhonySyllableTemplate()
) =
    syllableTemplate.createWord(
        PhonemeSequence(phonemes.toList()),
        makeSemanticsCore(speechPart)
    ) ?: throw TestAbortedException("Wrong word creation")

fun Word.withMorphemes(rootIdx: Int, vararg lengths: Int) =
    copy(
        morphemes = lengths.mapIndexed { i, it -> MorphemeData(it, listOf(), i == rootIdx) }
    )

fun Word.withMorphemes(vararg morphemes: MorphemeData) =
    copy(morphemes = morphemes.toList())

fun Word.withStaticCategories(vararg staticCategories: CategoryValue) =
    copy(
        semanticsCore = semanticsCore.copy(staticCategories = staticCategories.toSet())
    )

fun getPhonySyllableTemplate(): SyllableTemplate =
    SyllableValenceTemplate(
        ValencyPlace(PhonemeType.Consonant, 0.5),
        ValencyPlace(PhonemeType.Consonant, 0.5),
        ValencyPlace(PhonemeType.Consonant, 0.5),
        ValencyPlace(PhonemeType.Vowel, 1.0),
        ValencyPlace(PhonemeType.Consonant, 0.5),
        ValencyPlace(PhonemeType.Consonant, 0.5)
    )

fun makeSemanticsCore(speechPart: SpeechPart = SpeechPart.Noun) =
    SemanticsCore(MeaningCluster("phony"), TypedSpeechPart(speechPart), 1.0)
