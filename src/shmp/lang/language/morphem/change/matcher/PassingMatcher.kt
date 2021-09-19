package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.Syllables


object PassingMatcher : PositionMatcher {
    override fun test(syllables: Syllables) = true

    override fun toString() = "*"
}
