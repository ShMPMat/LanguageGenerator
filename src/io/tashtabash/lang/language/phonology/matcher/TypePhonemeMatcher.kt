package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType


class TypePhonemeMatcher(val phonemeType: PhonemeType): PhonemeMatcher() {
    override val name =
        phonemeType.name.take(1)

    override fun match(phoneme: Phoneme?) =
        phoneme?.type == phonemeType

    override fun match(changingPhoneme: ChangingPhoneme) =
        changingPhoneme is ChangingPhoneme.ExactPhoneme
                && changingPhoneme.phoneme.type == phonemeType

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        is CharacteristicPhonemeMatcher ->
            MulMatcher(this, other)
        is AbsentCharacteristicPhonemeMatcher ->
            MulMatcher(this, other)
        is ExactPhonemeMatcher ->
            if (other.phoneme.type == phonemeType)
                other
            else null
        is TypePhonemeMatcher ->
            if (this == other)
                this
            else null
        is ProsodyMatcher ->
            MulMatcher(this, other)
        is AbsentProsodyMatcher ->
            MulMatcher(this, other)
        is MulMatcher ->
            other * this
        PassingPhonemeMatcher, null -> this
        BorderPhonemeMatcher -> null
        else -> throw LanguageException("Cannot merge Phoneme matchers '$this' and '$other'")
    }
}
