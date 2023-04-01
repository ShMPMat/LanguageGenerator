package io.tashtabash.lang.language.morphem.change.matcher

import io.tashtabash.lang.language.phonology.Syllables


object PassingMatcher : PositionMatcher {
    override fun test(syllables: Syllables) = true

    override fun toString() = "*"
}
