package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType


class TypePhonemeMatcher(val phonemeType: PhonemeType): PhonemeMatcher() {
    override val name =
        phonemeType.name.take(1)

    override fun match(phoneme: Phoneme?) =
        phoneme?.type == phonemeType
}
