package shmp.language.derivation

import shmp.language.SpeechPart
import shmp.language.derivation.DerivationType.*
import shmp.random.SampleSpaceObject


enum class DerivationClass(val possibilities: List<Box>, val fromSpeechPart: SpeechPart, val toSpeechPart: SpeechPart) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Young, 1.0)), SpeechPart.Noun, SpeechPart.Noun),
    Augmentative(listOf(Box(Big, 3.0), Box(Old, 1.0)), SpeechPart.Noun, SpeechPart.Noun),
    Place(listOf(Box(NNPlace, 1.0)), SpeechPart.Noun, SpeechPart.Noun),
    Person(listOf(Box(NNPerson, 1.0)), SpeechPart.Noun, SpeechPart.Noun),
}

enum class DerivationType(val fromSpeechPart: SpeechPart, val toSpeechPart: SpeechPart) {
    Smallness(SpeechPart.Noun, SpeechPart.Noun),
    Young(SpeechPart.Noun, SpeechPart.Noun),

    Big(SpeechPart.Noun, SpeechPart.Noun),
    Old(SpeechPart.Noun, SpeechPart.Noun),

    NNPlace(SpeechPart.Noun, SpeechPart.Noun),
    NNPerson(SpeechPart.Noun, SpeechPart.Noun)
}

data class Box(val type: DerivationType?, override val probability: Double): SampleSpaceObject

val noType = listOf(Box(null, 1.0))
