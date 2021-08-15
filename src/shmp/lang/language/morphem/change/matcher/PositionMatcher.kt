package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.Phoneme


interface PositionMatcher {
    fun test(phoneme: Phoneme): Boolean
}
