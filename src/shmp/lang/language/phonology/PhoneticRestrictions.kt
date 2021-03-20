package shmp.lang.language.phonology

import shmp.lang.language.SpeechPart

class RestrictionsParadigm(val restrictionsMapper: Map<SpeechPart, PhoneticRestrictions>)

data class PhoneticRestrictions(
    val avgWordLength: Int,
    val initialSyllablePhonemes: Set<Phoneme>,
    val nucleusSyllablePhonemes: Set<Phoneme>,
    val finalSyllablePhonemes: Set<Phoneme>,
    val initialWordPhonemes: Set<Phoneme> = initialSyllablePhonemes,
    val finalWordPhonemes: Set<Phoneme> = finalSyllablePhonemes
)
