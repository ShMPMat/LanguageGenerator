package shmp.language.phonology

import shmp.language.SpeechPart

class RestrictionsParadigm(val restrictionsMapper: Map<SpeechPart, Restrictions>)

class Restrictions(
    val initialPhonemes: Set<PhonemeSequence>,
    val nucleusPhonemes: Set<PhonemeSequence>,
    val finalPhonemes: Set<PhonemeSequence>
)