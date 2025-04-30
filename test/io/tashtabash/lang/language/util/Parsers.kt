package io.tashtabash.lang.language.util

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.generator.GeneratedChange
import io.tashtabash.lang.language.category.realization.AffixCategoryApplicator
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.diachronicity.createPhonologicalRule
import io.tashtabash.lang.language.lexis.*
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

    return if (change.position == Position.End)
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

fun createAffixCategoryApplicator(vararg affixes: String): AffixCategoryApplicator {
    val parsedAffix =
        if (affixes.size == 1)
            createAffix(affixes[0])
        else
            createAffix(*affixes)

    return if (parsedAffix.templateChange.position == Position.Beginning)
        AffixCategoryApplicator(parsedAffix, CategoryRealization.Prefix)
    else
        AffixCategoryApplicator(parsedAffix, CategoryRealization.Suffix)
}

// Two different cases: with changing the stem and not changing the stem
fun createTemplateChange(templateChange: String): TemplateSingleChange = when {
    templateChange.contains("->") -> {
        val (matchers, substitutions) = templateChange.split("->")
            .map { it.trim() }
        val parsedSubstitutions = createTestPhonemeSubstitutions(substitutions)

        if (matchers[0] == '-') {
            val parsedMatchers = createTestPhonemeMatchers(matchers.drop(1))

            GeneratedChange(
                Position.End,
                parsedMatchers,
                parsedSubstitutions.take(parsedMatchers.size),
                parsedSubstitutions.drop(parsedMatchers.size)
                    .map { s -> s as ExactPhonemeSubstitution },
            )
        } else {
            val parsedMatchers = createTestPhonemeMatchers(matchers.dropLast(1))

            GeneratedChange(
                Position.Beginning,
                parsedMatchers,
                parsedSubstitutions.takeLast(parsedMatchers.size),
                parsedSubstitutions.dropLast(parsedMatchers.size)
                    .map { s -> s as ExactPhonemeSubstitution },
            )
        }.toTemplateSingleChange()
    }
    templateChange[0] == '-' -> {
        GeneratedChange(
            Position.End,
            listOf(),
            listOf(),
            templateChange.drop(1).map { ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme(it.toString())) }
        ).toTemplateSingleChange()
    }
    templateChange.last() == '-' -> {
        GeneratedChange(
            Position.Beginning,
            listOf(),
            listOf(),
            templateChange.dropLast(1).map { ExactPhonemeSubstitution(testPhonemeContainer.getPhoneme(it.toString())) }
        ).toTemplateSingleChange()
    }
    else -> throw Exception("Incorrect test affix format: '$templateChange'")
}

fun createPhonemes(phonemes: String) =
    phonemes.map { testPhonemeContainer.getPhoneme(it.toString()) }

fun createNoun(phonemes: String, syllableTemplate: SyllableTemplate = getPhonySyllableTemplate()) =
    createWord(phonemes, SpeechPart.Noun, syllableTemplate)

fun createIntransVerb(phonemes: String, syllableTemplate: SyllableTemplate = getPhonySyllableTemplate()) =
    createWord(phonemes, SpeechPart.Verb.toIntransitive(), syllableTemplate, setOf("intrans"))

fun createWord(
    phonemes: String,
    speechPart: SpeechPart,
    syllableTemplate: SyllableTemplate = getPhonySyllableTemplate(),
    tags: Set<SemanticsTag> = setOf()
) =
    createWord(createPhonemes(phonemes), speechPart.toDefault(), syllableTemplate, tags)

fun createWord(
    phonemes: String,
    speechPart: TypedSpeechPart,
    syllableTemplate: SyllableTemplate = getPhonySyllableTemplate(),
    tags: Set<String> = setOf()
): Word =
    createWord(createPhonemes(phonemes), speechPart, syllableTemplate, tags.map { SemanticsTag(it) }.toSet())

fun getTestPhoneme(symbol: String): Phoneme =
    testPhonemeContainer.getPhoneme(symbol)

val testPhonemeContainer = ImmutablePhonemeContainer(
    listOf(
        Phoneme("a", PhonemeType.Vowel, Front, Open, setOf(Voiced)),
        Phoneme("i", PhonemeType.Vowel, Front, Close, setOf(Voiced)),
        Phoneme("o", PhonemeType.Vowel, Back, CloseMid, setOf(Labialized, Voiced)),
        Phoneme("ɤ", PhonemeType.Vowel, Back, CloseMid, setOf(Voiced)),
        Phoneme("u", PhonemeType.Vowel, Back, Close, setOf(Labialized, Voiced)),
        Phoneme("ɯ", PhonemeType.Vowel, Back, Close, setOf(Voiced)),
        Phoneme("p", PhonemeType.Consonant, Bilabial, Stop, setOf()),
        Phoneme("b", PhonemeType.Consonant, Bilabial, Stop, setOf(Voiced)),
        Phoneme("t", PhonemeType.Consonant, Alveolar, Stop, setOf()),
        Phoneme("d", PhonemeType.Consonant, Alveolar, Stop, setOf(Voiced)),
        Phoneme("c", PhonemeType.Consonant, Palatal, Stop, setOf()),
        Phoneme("n", PhonemeType.Consonant, Alveolar, Nasal, setOf(Voiced)),
        Phoneme("r", PhonemeType.Consonant, Alveolar, Trill, setOf(Voiced)),
    ).flatMap { listOf(it, it.copy(it.symbol + it.symbol, modifiers = it.modifiers + PhonemeModifier.Long)) }
)
