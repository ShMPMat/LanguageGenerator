package io.tashtabash.lang.language.morphem.change.matcher

import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.Syllables


class PhonemeMatcher(val phoneme: Phoneme, private val isBeginning: Boolean) : PositionMatcher {
    override fun test(syllables: Syllables): Boolean {
        val phoneme = if (isBeginning) syllables[0][0] else syllables.last().last()

        return this.phoneme == phoneme
    }

    override fun toString() = phoneme.toString()
}
