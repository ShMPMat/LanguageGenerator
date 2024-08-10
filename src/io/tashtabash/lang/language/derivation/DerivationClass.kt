package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.derivation.DerivationType.*
import io.tashtabash.lang.language.lexis.Connotation
import io.tashtabash.lang.language.lexis.Connotations
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.random.UnwrappableSSO


enum class DerivationClass(
    val possibilities: List<Box>,
    val fromSpeechPart: SpeechPart,
    val toSpeechPart: SpeechPart,
    val shortName: String
) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Young, 1.0)), Noun, Noun, "Dim"),
    Augmentative(listOf(Box(Big, 3.0), Box(Old, 1.0)), Noun, Noun, "Aug"),

    PlaceFromNoun(listOf(Box(NNPlace, 1.0)), Noun, Noun, "Plc"),
    PersonFromNoun(listOf(Box(NNPerson, 1.0)), Noun, Noun, "Prs"),
    AbstractNounFromNoun(listOf(Box(NNAbstract, 1.0)), Noun, Noun, "Nom"),

    AbstractNounFromAdjective(listOf(Box(ANAbstract, 1.0)), Adjective, Noun, "Nom"),
    PlaceFromAdjective(listOf(Box(ANPlace, 1.0)), Adjective, Noun, "Plc"),

    BeingStateFromAdjective(listOf(Box(AVBeingState, 1.0)), Adjective, Verb, "Pred"),

    PlaceFromVerb(listOf(Box(VNPlace, 1.0)), Verb, Noun, "Pls"),
    PersonFromVerb(listOf(Box(VNPerson, 1.0)), Verb, Noun, "Prs"),
    AbstractNounFromVerb(listOf(Box(VNAbstract, 1.0)), Verb, Noun, "Nom")
}

enum class DerivationType(val fromSpeechPart: SpeechPart, val toSpeechPart: SpeechPart, val connotations: Connotations) {
    Smallness(Noun, Noun, Connotations(setOf(Connotation("small", 0.5)))),
    Young(Noun, Noun, Connotations(setOf(Connotation("young", 0.5)))),

    Big(Noun, Noun, Connotations(setOf(Connotation("big", 0.5)))),
    Old(Noun, Noun, Connotations(setOf(Connotation("old", 0.5)))),

    NNPlace(Noun, Noun, Connotations(setOf(Connotation("place", 1.0, true)))),
    NNPerson(Noun, Noun, Connotations(setOf(Connotation("person", 1.0, true)))),
    NNAbstract(Noun, Noun, Connotations(setOf(Connotation("abstract", 1.0, true)))),

    ANAbstract(Adjective, Noun, Connotations(setOf(Connotation("abstract", 1.0, true)))),
    ANPlace(Adjective, Noun, Connotations(setOf(Connotation("place", 1.0, true)))),

    AVBeingState(Adjective, Verb, Connotations(setOf(Connotation("action", 0.7, true)))),

    VNPlace(Verb, Noun, Connotations(setOf(Connotation("place", 1.0, true)))),
    VNPerson(Verb, Noun, Connotations(setOf(Connotation("person", 1.0, true)))),
    VNAbstract(Verb, Noun, Connotations(setOf(Connotation("abstract", 1.0, true))))
}

data class Box(val type: DerivationType?, override val probability: Double): UnwrappableSSO<DerivationType?>(type)

fun makeNoType(probability: Double) = listOf(Box(null, probability))
