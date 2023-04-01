package io.tashtabash.lang.language.phonology

import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.TypedSpeechPart

class RestrictionsParadigm(val restrictionsMapper: MutableMap<TypedSpeechPart, PhoneticRestrictions>) {
    fun getSpeechParts(speechPart: SpeechPart) = restrictionsMapper
        .keys.filter { it.type == speechPart }
}


data class PhoneticRestrictions(
    val syllableTemplate: SyllableTemplate,
    val avgWordLength: Int,
    val initialSyllablePhonemes: Set<Phoneme>,
    val nucleusSyllablePhonemes: Set<Phoneme>,
    val finalSyllablePhonemes: Set<Phoneme>,
    val initialWordPhonemes: Set<Phoneme> = initialSyllablePhonemes,
    val finalWordPhonemes: Set<Phoneme> = finalSyllablePhonemes
)
