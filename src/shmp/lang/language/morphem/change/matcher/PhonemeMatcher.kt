package shmp.lang.language.morphem.change.matcher

import shmp.lang.language.phonology.Phoneme


class PhonemeMatcher(val phoneme: Phoneme) : PositionMatcher {
    override fun test(phoneme: Phoneme) = this.phoneme == phoneme

    override fun toString() = phoneme.toString()
}
