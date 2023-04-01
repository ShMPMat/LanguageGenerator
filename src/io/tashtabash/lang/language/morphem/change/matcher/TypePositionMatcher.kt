package io.tashtabash.lang.language.morphem.change.matcher

import io.tashtabash.lang.language.phonology.PhonemeType
import io.tashtabash.lang.language.phonology.Syllables


class TypePositionMatcher(val type: PhonemeType, val isBeginning: Boolean) : PositionMatcher {
    override fun test(syllables: Syllables): Boolean {
        val phoneme = if (isBeginning) syllables[0][0] else syllables.last().last()

        return type == phoneme.type
    }

    override fun toString() = type.char.toString()
}
