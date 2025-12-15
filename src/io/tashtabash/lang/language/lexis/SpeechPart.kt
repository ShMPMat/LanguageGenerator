package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.lexis.SpeechPart.*


enum class SpeechPart {
    Noun,
    Verb,
    Adjective,
    Adverb,
    Numeral,
    Article,
    Particle,
    Adposition,
    PersonalPronoun,
    DeixisPronoun
}

val nominals = listOf(Noun, PersonalPronoun, DeixisPronoun)

fun isNominal(speechPart: TypedSpeechPart): Boolean =
    speechPart.type in nominals && speechPart.subtype != adnominalSubtype

data class TypedSpeechPart(val type: SpeechPart, val subtype: String = defaultSubtype) {
    override fun toString() = if (subtype == defaultSubtype)
        type.toString()
    else "$subtype $type"
}

fun SpeechPart.toDefault() = TypedSpeechPart(this, defaultSubtype)
fun SpeechPart.toAdnominal() = TypedSpeechPart(this, adnominalSubtype)
fun SpeechPart.toIntransitive() = TypedSpeechPart(this, intransitiveSubtype)

const val defaultSubtype = "Unspecified"
const val adnominalSubtype = "Adnominal"
const val intransitiveSubtype = "Intransitive"


val sameParadigmList = listOf(
    Noun to PersonalPronoun to 0.75,
    Noun to DeixisPronoun to 0.75,
    PersonalPronoun to DeixisPronoun to 0.1,

    Verb to Adjective to 0.05,

    //TODO Numerals
)
