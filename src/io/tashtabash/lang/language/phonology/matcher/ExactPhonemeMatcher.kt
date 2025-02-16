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

    override fun times(other: PhonemeMatcher?): Pair<PhonemeMatcher, Boolean>? = when (other) {
        is CharacteristicPhonemeMatcher ->
            (other * this)?.first?.let { it to false }
        is AbsentCharacteristicPhonemeMatcher ->
            (other * this)?.first?.let { it to false }
        is ExactPhonemeMatcher ->
            if (this == other)
                this to false
            else null
        is TypePhonemeMatcher ->
            (other * this)?.first?.let { it to false }
        is ProsodyMatcher ->
            other * this
        is AbsentProsodyMatcher ->
            (other * this)?.first?.let { it to false }
        is MulMatcher ->
            other * this
        PassingPhonemeMatcher, null -> this to false
        BorderPhonemeMatcher -> null
        else -> throw LanguageException("Cannot merge Phoneme matchers '$this' and '$other'")
    }
}
