package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeCharacteristic


class CharacteristicPhonemeMatcher(val characteristics: Set<PhonemeCharacteristic>): PhonemeMatcher() {
    constructor(vararg modifiers: PhonemeCharacteristic) : this(modifiers.toSet() )

    override val name =
        "[+${characteristics.map { it.toString() }.sorted().joinToString(",")}]"

    override fun match(phoneme: Phoneme?) =
        phoneme != null &&
                characteristics.all { it in phoneme.characteristics }

    override fun match(changingPhoneme: ChangingPhoneme) =
        changingPhoneme is ChangingPhoneme.ExactPhoneme
                && match(changingPhoneme.phoneme)

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        is CharacteristicPhonemeMatcher ->
            CharacteristicPhonemeMatcher(characteristics + other.characteristics)
        is AbsentCharacteristicPhonemeMatcher ->
            if (characteristics.none { it in other.characteristics })
                MulMatcher(this, other)
            else null
        is ExactPhonemeMatcher ->
            if (match(ChangingPhoneme.ExactPhoneme(other.phoneme)))
                other
            else null
        is TypePhonemeMatcher ->
            MulMatcher(other, this)
        is ProsodyMatcher ->
            MulMatcher(other, this)
        is AbsentProsodyMatcher ->
            MulMatcher(other, this)
        is MulMatcher ->
            other * this
        PassingPhonemeMatcher, null -> this
        BorderPhonemeMatcher -> null
        else -> throw LanguageException("Cannot merge Phoneme matchers '$this' and '$other'")
    }
}
