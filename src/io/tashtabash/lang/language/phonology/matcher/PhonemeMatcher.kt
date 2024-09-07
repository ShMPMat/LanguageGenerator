package io.tashtabash.lang.language.phonology.matcher

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeType


abstract class PhonemeMatcher {
    abstract val name: String

    abstract fun match(phoneme: Phoneme?): Boolean

    abstract fun match(changingPhoneme: ChangingPhoneme): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhonemeMatcher

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = name


}


fun createPhonemeMatcher(matcher: String, phonemeContainer: PhonemeContainer) = when (matcher) {
    "C" -> TypePhonemeMatcher(PhonemeType.Consonant)
    "V" -> TypePhonemeMatcher(PhonemeType.Vowel)
    "_" -> PassingPhonemeMatcher
    "$" -> BorderPhonemeMatcher
    else -> ExactPhonemeMatcher(phonemeContainer.getPhoneme(matcher))
}
