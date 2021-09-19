package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.Syllables


interface PositionMatcher {
    fun test(syllables: Syllables): Boolean
}
