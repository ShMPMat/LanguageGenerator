package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeModifier


class AbsentModifierPhonemeMatcher(val modifiers: Set<PhonemeModifier>): PhonemeMatcher() {
    constructor(vararg modifiers: PhonemeModifier) : this(modifiers.toSet() )

    override val name =
        "[-${modifiers.sorted().joinToString(",")}]"

    override fun match(phoneme: Phoneme?) =
        phoneme?.modifiers
            ?.none { it in modifiers }
            ?: false

    override fun match(changingPhoneme: ChangingPhoneme) =
        changingPhoneme is ChangingPhoneme.ExactPhoneme
                && match(changingPhoneme.phoneme)

    override fun times(other: PhonemeMatcher?): PhonemeMatcher? = when (other) {
        is AbsentModifierPhonemeMatcher ->
            AbsentModifierPhonemeMatcher(modifiers + other.modifiers)
        is ModifierPhonemeMatcher ->
            other * this
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
