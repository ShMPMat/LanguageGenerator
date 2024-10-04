package io.tashtabash.lang.language.util

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.language.diachronicity.createPhonologicalRule
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.lang.language.morphem.Prefix
import io.tashtabash.lang.language.morphem.Suffix
import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.morphem.change.TemplateSequenceChange
import io.tashtabash.lang.language.morphem.change.TemplateSingleChange
import io.tashtabash.lang.language.morphem.change.substitution.ExactPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.createPhonemeSubstitutions
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.phonology.ArticulationManner.*
import io.tashtabash.lang.language.phonology.ArticulationPlace.*
import io.tashtabash.lang.language.phonology.PhonemeModifier.*
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers


fun createTestPhonologicalRule(rule: String) =
    createPhonologicalRule(rule, testPhonemeContainer)

fun createTestPhonemeMatchers(matcher: String) =
    createPhonemeMatchers(matcher, testPhonemeContainer)

fun createTestPhonemeSubstitutions(substitutions: String) =
    createPhonemeSubstitutions(substitutions, testPhonemeContainer)


fun createAffix(vararg affixes: String): Affix {
    val change = TemplateSequenceChange(
        affixes.map { createAffix(it).templateChange }
    )

    return if (change.changes[0].position == Position.End)
        Suffix(change)
    else
        Prefix(change)
}

fun createAffix(affix: String): Affix {
    val templateChange = createTemplateChange(affix)

    return if (templateChange.position == Position.Beginning)
        Prefix(templateChange)
    else
        Suffix(templateChange)
}

// Two different cases: with changing the stem and not changing the stem
fun createTemplateChange(templateChange: String): TemplateSingleChange = when {
    templateChange.contains("->") -> {
        val (matchers, substitutions) = templateChange.split("->")
            .map { it.trim() }

        if (matchers[0] == '-')
            TemplateSingleChange(
                Position.End,
                createTestPhonemeMatchers(matchers.drop(1)),
                createTestPhonemeSubstitutions(substitutions.take(matchers.length - 1)),
                createTestPhonemeSubstitutions(substitutions.drop(matchers.length - 1))
                    .map { s -> s as ExactPhonemeSubstitution },
            )
        else
            TemplateSingleChange(
                Position.Beginning,
                createTestPhonemeMatchers(matchers.dropLast(1)),
                createTestPhonemeSubstitutions(substitutions.takeLast(matchers.length - 1)),
                createTestPhonemeSubstitutions(substitutions.dropLast(matchers.length - 1))
                    .map { s -> s as ExactPhonemeSubstitution },
            )
    }
    templateChange[0] == '-' -> {
        TemplateSingleChange(
            Position.End,
            listOf(),
            listOf(),
            templateChange.drop(1).map { ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme(it.toString())) }
        )
    }
    templateChange.last() == '-' -> {
        TemplateSingleChange(
            Position.Beginning,
            listOf(),
            listOf(),
            templateChange.dropLast(1).map { ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme(it.toString())) }
        )
    }
    else -> throw Exception("Incorrect test affix format: '$templateChange'")
}

fun createPhonemes(phonemes: String) =
    phonemes.map { testPhonemeContainer.getPhoneme(it.toString()) }

fun createNoun(phonemes: String, syllableTemplate: SyllableTemplate = getPhonySyllableTemplate()) =
    createWord(phonemes, SpeechPart.Noun, syllableTemplate)

fun createWord(
    phonemes: String,
    speechPart: SpeechPart,
    syllableTemplate: SyllableTemplate = getPhonySyllableTemplate()
) =
    createWord(createPhonemes(phonemes), speechPart, syllableTemplate)

val testPhonemeContainer = ImmutablePhonemeContainer(listOf(
    Phoneme("a", PhonemeType.Vowel, Front, Open, setOf(Voiced)),
    Phoneme("i", PhonemeType.Vowel, Front, Close, setOf(Voiced)),
    Phoneme("o", PhonemeType.Vowel, Back, CloseMid, setOf(Labialized, Voiced)),
    Phoneme("u", PhonemeType.Vowel, Back, Close, setOf(Labialized, Voiced)),
    Phoneme("p", PhonemeType.Consonant, Bilabial, Stop, setOf()),
    Phoneme("b", PhonemeType.Consonant, Bilabial, Stop, setOf(Voiced)),
    Phoneme("t", PhonemeType.Consonant, Alveolar, Stop, setOf()),
    Phoneme("d", PhonemeType.Consonant, Alveolar, Stop, setOf(Voiced)),
    Phoneme("c", PhonemeType.Consonant, Palatal, Stop, setOf()),
))
