package io.tashtabash.lang.language.morphem.change.matcher

import io.tashtabash.lang.language.phonology.Syllables


interface PositionMatcher {
    fun test(syllables: Syllables): Boolean
}
