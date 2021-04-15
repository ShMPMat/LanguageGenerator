package shmp.lang.language.derivation

import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.Noun
import shmp.lang.language.lexis.SpeechPart.Verb
import shmp.lang.language.derivation.DerivationType.*
import shmp.lang.language.lexis.Connotation
import shmp.lang.language.lexis.Connotations
import shmp.random.UnwrappableSSO


enum class DerivationClass(val possibilities: List<Box>, val fromSpeechPart: SpeechPart, val toSpeechPart: SpeechPart) {
    Diminutive(listOf(Box(Smallness, 1.0), Box(Young, 1.0)), Noun, Noun),
    Augmentative(listOf(Box(Big, 3.0), Box(Old, 1.0)), Noun, Noun),

    PlaceFromNoun(listOf(Box(NNPlace, 1.0)), Noun, Noun),
    PersonFromNoun(listOf(Box(NNPerson, 1.0)), Noun, Noun),
    AbstractNounFromNoun(listOf(Box(NNAbstract, 1.0)), Noun, Noun),
    PlaceFromVerb(listOf(Box(VNPlace, 1.0)), Verb, Noun),
    PersonFromVerb(listOf(Box(VNPerson, 1.0)), Verb, Noun),
    AbstractNounFromVerb(listOf(Box(VNAbstract, 1.0)), Verb, Noun)
}

enum class DerivationType(val fromSpeechPart: SpeechPart, val toSpeechPart: SpeechPart, val connotations: Connotations) {
    Smallness(Noun, Noun, Connotations(setOf(Connotation("small", 1.0)))),
    Young(Noun, Noun, Connotations(setOf(Connotation("young", 1.0)))),

    Big(Noun, Noun, Connotations(setOf(Connotation("big", 1.0)))),
    Old(Noun, Noun, Connotations(setOf(Connotation("old", 1.0)))),

    NNPlace(Noun, Noun, Connotations(setOf(Connotation("place", 0.1)))),
    NNPerson(Noun, Noun, Connotations(setOf(Connotation("person", 0.1)))),
    NNAbstract(Noun, Noun, Connotations(setOf(Connotation("abstract", 0.1)))),

    VNPlace(Verb, Noun, Connotations(setOf(Connotation("place", 0.1)))),
    VNPerson(Verb, Noun, Connotations(setOf(Connotation("person", 0.1)))),
    VNAbstract(Verb, Noun, Connotations(setOf(Connotation("abstract", 0.1))))
}//TODO NNAbstract

data class Box(val type: DerivationType?, override val probability: Double): UnwrappableSSO<DerivationType?>(type)

val noType = listOf(Box(null, 1.0))
