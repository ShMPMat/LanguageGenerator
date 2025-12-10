package io.tashtabash.lang.generator.supplement

import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.lexis.SpeechPart.*


data class AdditionalVerbType(val speechPart: TypedSpeechPart, val tag: SemanticsTag, val probability: Double)

val additionalVerbTypes = listOf(// For now all of them are intransitive, Arg = Stimulus, Obl = Experiencer
    AdditionalVerbType(TypedSpeechPart(Verb, "Perception"), SemanticsTag("perception"), .05),
    AdditionalVerbType(TypedSpeechPart(Verb, "Mental Activity"), SemanticsTag("mentalActivity"), .05),
    AdditionalVerbType(TypedSpeechPart(Verb, "Feeling"), SemanticsTag("feeling"), .05)
)
