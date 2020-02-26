package shmp.generator

import shmp.containers.PhonemeContainer
import shmp.language.phonology.PhoneticRestrictions
import shmp.language.phonology.Syllable

data class SyllableRestrictions(
    val phonemeContainer: PhonemeContainer,
    val phoneticRestrictions: PhoneticRestrictions,
    val canHaveFinal: Boolean = false,
    val shouldHaveInitial: Boolean = false,
    val shouldHaveFinal: Boolean = false,
    val prefix: List<Syllable> = listOf()
    ) {
}