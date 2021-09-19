package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.Phoneme
import shmp.lang.language.phonology.Syllables


class PhonemeMatcher(val phoneme: Phoneme, val isBeginning: Boolean) : PositionMatcher {
    override fun test(syllables: Syllables): Boolean {
        val phoneme = if (isBeginning) syllables[0][0] else syllables.last().last()

        return this.phoneme == phoneme
    }

    override fun toString() = phoneme.toString()
}
