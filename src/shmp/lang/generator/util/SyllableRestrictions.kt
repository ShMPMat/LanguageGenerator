package shmp.lang.generator.util

import shmp.lang.containers.PhonemeContainer
import shmp.lang.language.phonology.PhoneticRestrictions
import shmp.lang.language.phonology.Syllable
import shmp.lang.language.phonology.Syllables

data class SyllableRestrictions(
    val phonemeContainer: PhonemeContainer,
    val phoneticRestrictions: PhoneticRestrictions,
    val position: SyllablePosition,
    val prefix: Syllables = listOf(),
    var hasInitial: Boolean? = null,
    var hasFinal: Boolean? = null
    )

enum class SyllablePosition{
    Start,
    Middle,
    End
}
