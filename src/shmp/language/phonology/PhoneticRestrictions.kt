package shmp.language.phonology

import shmp.language.SpeechPart

class RestrictionsParadigm(val restrictionsMapper: Map<SpeechPart, PhoneticRestrictions>)

class PhoneticRestrictions(
    val initialSyllablePhonemes: Set<PhonemeSequence>,
    val nucleusSyllablePhonemes: Set<PhonemeSequence>,
    val finalSyllablePhonemes: Set<PhonemeSequence>,
    val initialWordPhonemes: Set<PhonemeSequence> = initialSyllablePhonemes,
    val finalWordPhonemes: Set<PhonemeSequence> = finalSyllablePhonemes

)