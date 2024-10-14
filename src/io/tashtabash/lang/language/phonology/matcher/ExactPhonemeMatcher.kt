package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme


class ExactPhonemeMatcher(val phoneme: Phoneme): PhonemeMatcher() {
    override val name =
        phoneme.symbol

    override fun match(phoneme: Phoneme?) =
        phoneme == this.phoneme

    override fun match(changingPhoneme: ChangingPhoneme) =
        changingPhoneme is ChangingPhoneme.ExactPhoneme
                && changingPhoneme.phoneme == phoneme

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        is ModifierPhonemeMatcher ->
            if (other.match(ChangingPhoneme.ExactPhoneme(phoneme)))
                this
            else null
        is AbsentModifierPhonemeMatcher ->
            if (other.match(ChangingPhoneme.ExactPhoneme(phoneme)))
                this
            else null
        is ExactPhonemeMatcher ->
            if (this == other)
                this
            else null
        is TypePhonemeMatcher ->
            if (phoneme.type == other.phonemeType)
                this
            else null
        is MulMatcher ->
            other * this
        PassingPhonemeMatcher, null -> this
        BorderPhonemeMatcher -> null
        else -> throw LanguageException("Cannot merge Phoneme matchers '$this' and '$other'")
    }
}
