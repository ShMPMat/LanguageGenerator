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
                && changingPhoneme.prosody.let { it != null && prosody.all { p -> p in it } }

    override fun times(other: PhonemeMatcher?): Pair<PhonemeMatcher, Boolean>? = when (other) {
        is CharacteristicPhonemeMatcher ->
            MulMatcher(this, other) to true
        is AbsentCharacteristicPhonemeMatcher ->
            MulMatcher(this, other) to true
        is ExactPhonemeMatcher ->
            MulMatcher(other, this) to true
        is TypePhonemeMatcher ->
            MulMatcher(other, this) to true
        is ProsodyMatcher ->
            ProsodyMatcher(prosody + other.prosody) to ((prosody + other.prosody).size != prosody.size)
        is AbsentProsodyMatcher ->
            if (prosody.none { it in other.prosody })
                MulMatcher(this, other) to true
            else null
        is MulMatcher ->
            other * this
        PassingPhonemeMatcher, null -> this to false
        BorderPhonemeMatcher -> null
        else -> throw LanguageException("Cannot merge Phoneme matchers '$this' and '$other'")
    }
}
