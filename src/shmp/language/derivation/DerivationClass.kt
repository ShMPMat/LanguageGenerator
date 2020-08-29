package shmp.language.derivation

import shmp.language.SpeechPart
import shmp.language.derivation.DerivationType.*
import shmp.random.SampleSpaceObject


enum class DerivationClass(val possibilities: List<Box>, val speechPart: SpeechPart) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Young, 1.0)), SpeechPart.Noun),
    Augmentative(listOf(Box(Big, 3.0), Box(Old, 1.0)), SpeechPart.Noun),
    Place(listOf(Box(NNPlace, 1.0)), SpeechPart.Noun),
    Person(listOf(Box(NNPerson, 1.0)), SpeechPart.Noun),
}

enum class DerivationType {
    Smallness,
    Young,

    Big,
    Old,

    NNPlace,
    NNPerson,

    Passing
}

data class Box(val type: DerivationType, override val probability: Double): SampleSpaceObject

val noType = listOf(Box(Passing, 1.0))
