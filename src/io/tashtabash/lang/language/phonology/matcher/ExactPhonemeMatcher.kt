package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.phonology.Phoneme


class ExactPhonemeMatcher(val phoneme: Phoneme): PhonemeMatcher() {
    override val name =
        phoneme.symbol

    override fun match(phoneme: Phoneme?) =
        phoneme == this.phoneme
}
