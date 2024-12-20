package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme


class MulMatcher(val matchers: List<PhonemeMatcher>): PhonemeMatcher() {
    constructor(vararg matchers: PhonemeMatcher) : this(matchers.asList())

    override val name =
        matchers.joinToString("", "(", ")")

    override fun match(phoneme: Phoneme?) =
        matchers.all { it.match(phoneme) }

    override fun match(changingPhoneme: ChangingPhoneme) =
        matchers.all { it.match(changingPhoneme) }

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        is MulMatcher ->
            if (matchers[0] !is TypePhonemeMatcher && other.matchers[0] is TypePhonemeMatcher)
                other * this // Preserves the invariant "a type matcher should be the first matcher"
            else {
                var newMatcher: MulMatcher? = this
                for (otherMatcher in other.matchers)
                    newMatcher = newMatcher?.mergeNonMulMatcher(otherMatcher)

                newMatcher?.matchers
                    ?.distinct()
                    ?.let {
                        if (it.size == 1)
                            it[0]
                        else
                            MulMatcher(it)
                    }
            }
        PassingPhonemeMatcher, null -> this
        BorderPhonemeMatcher -> null
        else -> this * MulMatcher(other)
    }

    private fun mergeNonMulMatcher(other: PhonemeMatcher): MulMatcher? {
        if (other is MulMatcher)
            throw LanguageException("Can't merge a MulMatcher")

        var isOtherMerged = false
        val newMatchers = matchers.map {
            val newMatcher = (it * other)
                ?: return null

            if (newMatcher is MulMatcher)
                it
            else {
                isOtherMerged = true
                newMatcher
            }
        }

        return MulMatcher(
            newMatchers + if (isOtherMerged) listOf() else listOf(other)
        )
    }

    override fun any(predicate: (PhonemeMatcher) -> Boolean): Boolean =
        super.any(predicate) || matchers.any { it.any(predicate) }
}
