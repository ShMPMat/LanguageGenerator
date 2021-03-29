package shmp.lang.language.derivation

import shmp.lang.language.SpeechPart
import shmp.lang.language.SpeechPart.Noun
import shmp.lang.language.SpeechPart.Verb
import shmp.lang.language.derivation.DerivationType.*
import shmp.random.UnwrappableSSO


enum class DerivationClass(val possibilities: List<Box>, val fromSpeechPart: SpeechPart, val toSpeechPart: SpeechPart) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Young, 1.0)), Noun, Noun),
    Augmentative(listOf(Box(Big, 3.0), Box(Old, 1.0)), Noun, Noun),

    PlaceFromNoun(listOf(Box(NNPlace, 1.0)), Noun, Noun),
    PersonFromNoun(listOf(Box(NNPerson, 1.0)), Noun, Noun),
    PlaceFromVerb(listOf(Box(VNPlace, 1.0)), Verb, Noun),
    PersonFromVerb(listOf(Box(VNPerson, 1.0)), Verb, Noun),
    AbstractNounFromVerb(listOf(Box(VNAbstract, 1.0)), Verb, Noun)
}

enum class DerivationType(val fromSpeechPart: SpeechPart, val toSpeechPart: SpeechPart) {
    Smallness(Noun, Noun),
    Young(Noun, Noun),

    Big(Noun, Noun),
    Old(Noun, Noun),

    NNPlace(Noun, Noun),
    NNPerson(Noun, Noun),

    VNPlace(Verb, Noun),
    VNPerson(Verb, Noun),
    VNAbstract(Verb, Noun)
}//TODO NNAbstract

data class Box(val type: DerivationType?, override val probability: Double): UnwrappableSSO<DerivationType?>(type)

val noType = listOf(Box(null, 1.0))
