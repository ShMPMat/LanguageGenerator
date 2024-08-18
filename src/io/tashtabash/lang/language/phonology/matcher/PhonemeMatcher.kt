package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.phonology.Phoneme


abstract class PhonemeMatcher {
    abstract val name: String

    abstract fun match(phoneme: Phoneme?): Boolean

    override fun toString() = name
}
