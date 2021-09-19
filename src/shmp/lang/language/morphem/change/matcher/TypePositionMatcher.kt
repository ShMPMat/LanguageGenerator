package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.PhonemeType
import shmp.lang.language.phonology.Syllables


class TypePositionMatcher(val type: PhonemeType, val isBeginning: Boolean) : PositionMatcher {
    override fun test(syllables: Syllables): Boolean {
        val phoneme = if (isBeginning) syllables[0][0] else syllables.last().last()

        return type == phoneme.type
    }

    override fun toString() = type.char.toString()
}
