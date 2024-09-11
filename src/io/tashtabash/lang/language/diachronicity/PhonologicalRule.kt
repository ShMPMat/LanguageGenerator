package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.createPhonemeSubstitution
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatcher


data class PhonologicalRule(
    val precedingMatchers: List<PhonemeMatcher>,
    val targetMatchers: List<PhonemeMatcher>,
    val followingMatchers: List<PhonemeMatcher>,
    val resultingPhonemes: List<PhonemeSubstitution>,
) {
    init {
        if (targetMatchers.size != resultingPhonemes.size)
            throw LanguageException("The number of targetMatchers and resultingPhonemes must be the same")
    }

    val matchers: List<PhonemeMatcher>
        get() = precedingMatchers + targetMatchers + followingMatchers

    fun mirror() = PhonologicalRule(
        followingMatchers.reversed(),
        targetMatchers.reversed(),
        precedingMatchers.reversed(),
        resultingPhonemes.reversed()
    )

    override fun toString() = targetMatchers.joinToString("") +
            " -> ${resultingPhonemes.joinToString("")}" +
            " / ${precedingMatchers.joinToString("")}" +
            " _ ${followingMatchers.joinToString("")}"
}


fun createPhonologicalRule(rule: String, phonemeContainer: PhonemeContainer) = PhonologicalRule(
    rule.dropWhile { it != '/' }
        .drop(1)
        .dropLastWhile { it != '_' }
        .dropLast(1)
        .replace(" ", "")
        .map { createPhonemeMatcher(it.toString(), phonemeContainer) },
    rule.dropLastWhile { it != '>' }
        .dropLast(2)
        .replace(" ", "")
        .map { createPhonemeMatcher(it.toString(), phonemeContainer) },
    rule.dropWhile { it != '/' }
        .dropWhile { it != '_' }
        .drop(1)
        .replace(" ", "")
        .map { createPhonemeMatcher(it.toString(), phonemeContainer) },
    rule.dropWhile { it != '>' }
        .drop(1)
        .dropLastWhile { it != '/' }
        .dropLast(1)
        .replace(" ", "")
        .map { createPhonemeSubstitution(it.toString(), phonemeContainer) },
)
