package shmp.lang.generator.util

import shmp.lang.containers.PhonemeContainer
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.lang.language.phonology.Syllable

data class SyllableRestrictions(
    val phonemeContainer: PhonemeContainer,
    val phoneticRestrictions: PhoneticRestrictions,
    val position: SyllablePosition,
    val prefix: List<Syllable> = listOf(),
    var shouldHaveInitial: Boolean = false,
    var shouldHaveFinal: Boolean = false
    )

enum class SyllablePosition{
    Start,
    Middle,
    End
}
