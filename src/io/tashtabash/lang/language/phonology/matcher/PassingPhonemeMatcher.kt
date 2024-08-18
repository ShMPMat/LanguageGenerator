package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.language.phonology.Phoneme


object PassingPhonemeMatcher: PhonemeMatcher() {
    override val name =
        "*"

    override fun match(phoneme: Phoneme?) =
        true
}
