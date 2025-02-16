package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeCharacteristic


class AbsentCharacteristicPhonemeMatcher(val characteristics: Set<PhonemeCharacteristic>): PhonemeMatcher() {
    constructor(vararg modifiers: PhonemeCharacteristic) : this(modifiers.toSet() )

    override val name =
        "[-${characteristics.map { it.toString() }.sorted().joinToString(",")}]"

    override fun match(phoneme: Phoneme?) =
        phoneme?.characteristics
            ?.none { it in characteristics }
            ?: false

    override fun match(changingPhoneme: ChangingPhoneme) =
        changingPhoneme is ChangingPhoneme.ExactPhoneme
                && match(changingPhoneme.phoneme)

    override fun times(other: PhonemeMatcher?): Pair<PhonemeMatcher, Boolean>? = when (other) {
        is AbsentCharacteristicPhonemeMatcher ->
            AbsentCharacteristicPhonemeMatcher(characteristics + other.characteristics) to
                    ((characteristics + other.characteristics).size != characteristics.size)
        is CharacteristicPhonemeMatcher ->
            other * this
        is ExactPhonemeMatcher ->
            if (match(ChangingPhoneme.ExactPhoneme(other.phoneme)))
                other to true
            else null
        is TypePhonemeMatcher ->
            MulMatcher(other, this) to true
        is ProsodyMatcher ->
            MulMatcher(other, this) to true
        is AbsentProsodyMatcher ->
            MulMatcher(other, this) to true
        is MulMatcher ->
            other * this
        PassingPhonemeMatcher, null -> this to false
        BorderPhonemeMatcher -> null
        else -> throw LanguageException("Cannot merge Phoneme matchers '$this' and '$other'")
    }
}
