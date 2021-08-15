package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.Phoneme
import shmp.lang.language.phonology.PhonemeType


class TypePositionMatcher(val type: PhonemeType) : PositionMatcher {
    override fun test(phoneme: Phoneme) = type == phoneme.type

    override fun toString() = type.char.toString()
}
