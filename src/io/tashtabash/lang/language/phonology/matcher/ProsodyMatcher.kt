package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.prosody.Prosody


class ProsodyMatcher(val prosody: Set<Prosody>): PhonemeMatcher() {
    constructor(vararg modifiers: Prosody) : this(modifiers.toSet() )

    override val name =
        "{+${prosody.sorted().joinToString(",")}}"

    override fun match(phoneme: Phoneme?) =
        false

    override fun match(changingPhoneme: ChangingPhoneme) =
        changingPhoneme is ChangingPhoneme.ExactPhoneme
                && changingPhoneme.prosody != null
                && prosody.all { it in changingPhoneme.prosody }

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        is ModifierPhonemeMatcher ->
            MulMatcher(this, other)
        is AbsentModifierPhonemeMatcher ->
            MulMatcher(this, other)
        is ExactPhonemeMatcher ->
            MulMatcher(other, this)
        is TypePhonemeMatcher ->
            MulMatcher(other, this)
        is ProsodyMatcher ->
            ProsodyMatcher(prosody + other.prosody)
        is AbsentProsodyMatcher ->
            if (prosody.none { it in other.prosody })
                MulMatcher(this, other)
            else null
        is MulMatcher ->
            other * this
        PassingPhonemeMatcher, null -> this
        BorderPhonemeMatcher -> null
        else -> throw LanguageException("Cannot merge Phoneme matchers '$this' and '$other'")
    }
}
