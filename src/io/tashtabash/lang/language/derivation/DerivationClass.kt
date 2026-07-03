package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.derivation.DerivationType.*
import io.tashtabash.lang.language.lexis.Connotation
import io.tashtabash.lang.language.lexis.Connotations
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.lexis.toInf
import io.tashtabash.lang.language.lexis.toIntransitive
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.withProb


enum class DerivationClass(
    val possibilities: List<GenericSSO<DerivationType>>,
    val fromSpeechPart: SpeechPart,
    val toSpeechPart: TypedSpeechPart,
    val shortName: String
) {
    Diminutive(listOf(Smallness.withProb(1.0), Young.withProb(1.0)), Noun, Noun.toDefault(), "Dim"),
    Augmentative(listOf(Big.withProb(3.0), Old.withProb(1.0)), Noun, Noun.toDefault(), "Aug"),

    PlaceFromNoun(listOf(NNPlace.withProb(1.0)), Noun, Noun.toDefault(), "Plc"),
    PersonFromNoun(listOf(NNPerson.withProb(1.0)), Noun, Noun.toDefault(), "Prs"),
    AbstractNounFromNoun(listOf(NNAbstract.withProb(1.0)), Noun, Noun.toDefault(), "Nmlz"),

    AbstractNounFromAdjective(listOf(ANAbstract.withProb(1.0)), Adjective, Noun.toDefault(), "Nmlz"),
    PlaceFromAdjective(listOf(ANPlace.withProb(1.0)), Adjective, Noun.toDefault(), "Plc"),

    BeingStateFromAdjective(listOf(AVBeingState.withProb(1.0)), Adjective, Verb.toIntransitive(), "Pred"),

    PlaceFromVerb(listOf(VNPlace.withProb(1.0)), Verb, Noun.toDefault(), "Pls"),
    PersonFromVerb(listOf(VNPerson.withProb(1.0)), Verb, Noun.toDefault(), "Prs"),
    AbstractNounFromVerb(listOf(VNAbstract.withProb(1.0)), Verb, Noun.toDefault(), "Nmlz"),

    InfinitiveVerb(listOf(Inf.withProb(1.0)), Verb, Verb.toInf(), "Inf")
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
    VNAbstract(Verb, Noun, Connotations(setOf(Connotation("abstract", 1.0, true)))),

    Inf(Verb, Verb, Connotations()),
}

fun makeNoType(probability: Double): GenericSSO<DerivationType?> = null.withProb(probability)
