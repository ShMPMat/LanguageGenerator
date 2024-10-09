package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.utils.cartesianProduct


class MulMatcher(val matchers: List<PhonemeMatcher>): PhonemeMatcher() {
    constructor(vararg matchers: PhonemeMatcher) : this(matchers.asList())

    override val name =
        matchers.drop(1).joinToString("", "(", ")")

    override fun match(phoneme: Phoneme?) =
        matchers.all { it.match(phoneme) }

    override fun match(changingPhoneme: ChangingPhoneme) =
        matchers.all { it.match(changingPhoneme) }

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        is MulMatcher -> {
            val haveIncompatibleMatchers = cartesianProduct(matchers, other.matchers)
                .any { (f, s) -> f * s == null }
            if (haveIncompatibleMatchers)
                null
            else
                MulMatcher(matchers + other.matchers.filter { it !in matchers })
        }
        PassingPhonemeMatcher, null -> this
        BorderPhonemeMatcher -> null
        else -> this * MulMatcher(other)
    }
}
