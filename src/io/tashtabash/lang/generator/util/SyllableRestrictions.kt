package io.tashtabash.lang.generator.util

import io.tashtabash.lang.containers.PhonemeContainer
import io.tashtabash.lang.language.phonology.PhoneticRestrictions
import io.tashtabash.lang.language.phonology.Syllable
import io.tashtabash.lang.language.phonology.Syllables

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
