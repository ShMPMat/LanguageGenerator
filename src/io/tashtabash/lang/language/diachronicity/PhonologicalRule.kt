package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.morphem.change.substitution.EpenthesisSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.createPhonemeSubstitutions
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.createPhonemeMatchers


data class PhonologicalRule(
    val precedingMatchers: List<PhonemeMatcher>,
    val targetMatchers: List<PhonemeMatcher>,
    val followingMatchers: List<PhonemeMatcher>,
    val resultingPhonemes: List<PhonemeSubstitution>,
    val allowSyllableStructureChange: Boolean = false
) {
    init {
        if (targetMatchers.size != resultingPhonemes.count { it !is EpenthesisSubstitution })
            throw LanguageException("The number of targetMatchers and non-epenthesis resultingPhonemes must be equal")
    }

    constructor(
        matchers: List<PhonemeMatcher>,
        precedingLength: Int,
        followingLength: Int,
        resultingPhonemes: List<PhonemeSubstitution>,
        allowSyllableStructureChange: Boolean
    ) : this(
        matchers.take(precedingLength),
        matchers.drop(precedingLength).dropLast(followingLength),
        matchers.takeLast(followingLength),
        resultingPhonemes,
        allowSyllableStructureChange
    )

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
            " _ ${followingMatchers.joinToString("")}" +
            if (allowSyllableStructureChange) "!" else ""
}


fun createPhonologicalRule(rule: String, phonemeContainer: PhonemeContainer): PhonologicalRule {
    val allowSyllableStructureChange = rule.last() == '!'
    val clearedRule = rule.dropLastWhile { it == '!' }

    return PhonologicalRule(
        clearedRule.dropWhile { it != '/' }
            .drop(1)
            .dropLastWhile { it != '_' }
            .dropLast(1)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropLastWhile { it != '>' }
            .dropLast(2)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropWhile { it != '/' }
            .dropWhile { it != '_' }
            .drop(1)
            .replace(" ", "")
            .let { createPhonemeMatchers(it, phonemeContainer) },
        clearedRule.dropWhile { it != '>' }
            .drop(1)
            .dropLastWhile { it != '/' }
            .dropLast(1)
            .replace(" ", "")
            .let { createPhonemeSubstitutions(it, phonemeContainer) },
        allowSyllableStructureChange
    )
}
