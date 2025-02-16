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

    override fun times(other: PhonemeMatcher?): Pair<PhonemeMatcher, Boolean>? = when (other) {
        is MulMatcher ->
            if (matchers[0] !is TypePhonemeMatcher && other.matchers[0] is TypePhonemeMatcher)
                other * this // Preserves the invariant "a type matcher should be the first matcher"
            else {
                val newMatcher = other.matchers.fold(this to false) { a: Pair<MulMatcher, Boolean>?, m ->
                    a?.first?.mergeNonMulMatcher(m)
                }

                newMatcher?.let { (matcher, isNarrowed) ->
                    matcher.matchers
                        .distinct()
                        .let {
                            if (it.size == 1)
                                it[0]
                            else
                                MulMatcher(it)
                        } to isNarrowed
                }
            }
        PassingPhonemeMatcher, null -> this to false
        BorderPhonemeMatcher -> null
        else -> this * MulMatcher(other)
    }

    private fun mergeNonMulMatcher(other: PhonemeMatcher): Pair<MulMatcher, Boolean>? {
        if (other is MulMatcher)
            throw LanguageException("Can't merge a MulMatcher")

        var isOtherMerged = false
        var isNarrowed = false
        val newMatchers = matchers.map {
            val newMatcher = (it * other)
                ?: return null

            if (newMatcher.first is MulMatcher)
                it
            else {
                isOtherMerged = true
                isNarrowed = newMatcher.second
                newMatcher.first
            }
        }

        return MulMatcher(
            newMatchers + if (isOtherMerged) listOf() else listOf(other)
        ) to (!isOtherMerged || isNarrowed)
    }

    override fun any(predicate: (PhonemeMatcher) -> Boolean): Boolean =
        super.any(predicate) || matchers.any { it.any(predicate) }
}
