package io.tashtabash.lang.language.util

import io.tashtabash.lang.containers.ImmutablePhonemeContainer
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.createPhonologicalRule
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.lang.language.morphem.Prefix
import io.tashtabash.lang.language.morphem.Suffix
import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.morphem.change.TemplateSequenceChange
import io.tashtabash.lang.language.morphem.change.TemplateSingleChange
import io.tashtabash.lang.language.morphem.change.substitution.DeletingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.ExactPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PassingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.createPhonemeSubstitution
import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.matcher.PassingPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatcher


fun createTestPhonologicalRule(rule: String) = createPhonologicalRule(
    rule,
    testPhonemeContainer
)

fun createTestPhonemeMatcher(matcher: Char) = createPhonemeMatcher(
    matcher.toString(),
    testPhonemeContainer
)

fun createTestPhonemeSubstitution(substitution: Char) = createPhonemeSubstitution(
    substitution.toString(),
    testPhonemeContainer
)


fun createAffix(vararg affixes: String): Affix {
    val change = TemplateSequenceChange(
        affixes.map { createAffix(it).templateChange }
    )

    return if (change.changes[0].position == Position.End)
        Suffix(change)
    else
        Prefix(change)
}

// Two different cases: with changing the stem and not changing the stem
fun createAffix(affix: String) = when {
    affix.contains("->") -> {
        val (matchers, substitutions) = affix.split("->")
            .map { it.trim() }

        if (matchers[0] == '-')
            Suffix(TemplateSingleChange(
                Position.End,
                matchers.drop(1)
                    .map { createTestPhonemeMatcher(it) },
                substitutions.take(matchers.length - 1)
                    .map { createTestPhonemeSubstitution(it) },
                substitutions.drop(matchers.length - 1)
                    .map { createTestPhonemeSubstitution(it) as ExactPhonemeSubstitution },
            ))
        else
            Prefix(TemplateSingleChange(
                Position.Beginning,
                matchers.dropLast(1)
                    .map { createTestPhonemeMatcher(it) },
                substitutions.takeLast(matchers.length - 1)
                    .map { createTestPhonemeSubstitution(it) },
                substitutions.dropLast(matchers.length - 1)
                    .map { createTestPhonemeSubstitution(it) as ExactPhonemeSubstitution },
            ))
    }
    affix[0] == '-' -> {
        Suffix(TemplateSingleChange(
            Position.End,
            listOf(),
            listOf(),
            affix.drop(1).map { ExactPhonemeSubstitution(makePhoneme(it)) }
        ))
    }
    affix.last() == '-' -> {
        Prefix(TemplateSingleChange(
            Position.Beginning,
            listOf(),
            listOf(),
            affix.dropLast(1).map { ExactPhonemeSubstitution(makePhoneme(it)) }
        ))
    }
    else -> throw Exception("Incorrect test affix format: '$affix'")
}

fun createPhonemeContainer(phonemes: String) =
    createPhonemes(phonemes)
        .distinct()
        .let {
            ImmutablePhonemeContainer(it)
        }

fun createPhonemes(phonemes: String) =
    phonemes.map {
        makePhoneme(it)
    }

fun makePhoneme(symbol: Char) = makePhoneme(
    symbol.toString(),
    if (symbol in testVowelChars) PhonemeType.Vowel else PhonemeType.Consonant
)

fun createNoun(phonemes: String) =
    createNoun(createPhonemes(phonemes))

val testVowelChars = listOf('a', 'e', 'i', 'o', 'u')
val testPhonemeContainer = createPhonemeContainer("aoubcit")
