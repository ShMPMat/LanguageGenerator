package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier
import io.tashtabash.lang.language.phonology.PhonemeType
import kotlin.math.max


abstract class PhonemeMatcher {
    abstract val name: String

    abstract fun match(phoneme: Phoneme?): Boolean

    abstract fun match(changingPhoneme: ChangingPhoneme): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhonemeMatcher

        if (name != other.name) return false

        return true
    }

    abstract operator fun times(other: PhonemeMatcher?): PhonemeMatcher?

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = name
}

fun createPhonemeMatchers(matchers: String, phonemeContainer: PhonemeContainer): List<PhonemeMatcher> {
    var currentPostfix = matchers
    val resultMatchers = mutableListOf<PhonemeMatcher>()

    while (currentPostfix.isNotEmpty()) {
        val token = if (currentPostfix[0] == '[')
            currentPostfix.takeWhile { it != ']' } + ']'
        else
            currentPostfix.take(1)

        resultMatchers += createPhonemeMatcher(token, phonemeContainer)

        currentPostfix = currentPostfix.drop(token.length)
    }

    return resultMatchers
}

fun createPhonemeMatcher(matcher: String, phonemeContainer: PhonemeContainer) = when {
    matcher == "C" -> TypePhonemeMatcher(PhonemeType.Consonant)
    matcher == "V" -> TypePhonemeMatcher(PhonemeType.Vowel)
    matcher == "_" -> PassingPhonemeMatcher
    matcher == "$" -> BorderPhonemeMatcher
    modifierRegex.matches(matcher) -> ModifierPhonemeMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { PhonemeModifier.valueOf(it) }
            .toSet()
    )
    absentModifierRegex.matches(matcher) -> AbsentModifierPhonemeMatcher(
        matcher.drop(2)
            .dropLast(1)
            .split(",")
            .map { PhonemeModifier.valueOf(it) }
            .toSet()
    )
    else -> {
        val phoneme = phonemeContainer.getPhonemeOrNull(matcher)
            ?: throw LanguageException("cannot create a matcher for symbol '$matcher'")

        ExactPhonemeMatcher(phoneme)
    }
}

private val modifierRegex = "\\[\\+.*]".toRegex()
private val absentModifierRegex = "\\[-.*]".toRegex()


fun unitePhonemeMatchers(first: List<PhonemeMatcher>, second: List<PhonemeMatcher>): List<PhonemeMatcher?> {
    val newMatchersLength = max(first.size, second.size)

    return (0 until newMatchersLength).map { j ->
        val curFirst = first.getOrNull(j)
        val curSecond = second.getOrNull(j)

        curFirst?.times(curSecond)
            ?: curSecond?.times(curFirst)
    }
}
