package shmp.lang.language.lexis


enum class SpeechPart {
    Noun,
    Verb,
    Adjective,
    Adverb,
    Numeral,
    Article,
    Particle,
    PersonalPronoun,
    DeixisPronoun
}

data class TypedSpeechPart(val type: SpeechPart, val subtype: String) {
    override fun toString() = if (subtype == defaultSubtype)
        type.toString()
    else "$subtype $type"
}

fun SpeechPart.toUnspecified() = TypedSpeechPart(this, defaultSubtype)

const val defaultSubtype = "unspecified"
const val adnominalSubtype = "Adnominal"
