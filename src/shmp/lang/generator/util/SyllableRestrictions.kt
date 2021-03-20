package shmp.lang.generator.util

import shmp.lang.containers.PhonemeContainer
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.lang.language.phonology.Syllable

data class SyllableRestrictions(
    val phonemeContainer: PhonemeContainer,
    val phoneticRestrictions: PhoneticRestrictions,
    val position: SyllablePosition,
    val shouldHaveInitial: Boolean = false,
    val shouldHaveFinal: Boolean = false,
    val prefix: List<Syllable> = listOf()
    )

enum class SyllablePosition{
    Start,
    Middle,
    End
}
