package shmp.language.phonology

import shmp.language.SpeechPart

class RestrictionsParadigm(restrictionsMapper: Map<SpeechPart, Restrictions>)

class Restrictions(
    initialPhonemes: Set<PhonemeSequence>,
    nucleusPhonemes: Set<PhonemeSequence>,
    finalPhonemes: Set<PhonemeSequence>
)