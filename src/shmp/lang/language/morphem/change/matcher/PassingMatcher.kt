package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.Phoneme


object PassingMatcher : PositionMatcher {
    override fun test(phoneme: Phoneme) = true

    override fun toString() = "*"
}
