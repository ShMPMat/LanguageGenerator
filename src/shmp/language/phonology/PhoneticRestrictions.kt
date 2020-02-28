package shmp.language.phonology

import shmp.language.SpeechPart

class RestrictionsParadigm(val restrictionsMapper: Map<SpeechPart, PhoneticRestrictions>)

class PhoneticRestrictions(
    val initialSyllablePhonemes: Set<Phoneme>,
    val nucleusSyllablePhonemes: Set<Phoneme>,
    val finalSyllablePhonemes: Set<Phoneme>,
    val initialWordPhonemes: Set<Phoneme> = initialSyllablePhonemes,
    val finalWordPhonemes: Set<Phoneme> = finalSyllablePhonemes

)