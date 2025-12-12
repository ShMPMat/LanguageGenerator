package io.tashtabash.lang.generator

import io.tashtabash.lang.language.lexis.SpeechPart.Noun
import io.tashtabash.lang.language.lexis.SpeechPart.PersonalPronoun
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.transformer.*
import io.tashtabash.random.singleton.testProbability


class TransformerGenerator {
    fun generateTransformers(): List<Pair<SyntaxNodeMatcher, Transformer>> = listOfNotNull(
        // Word order
        (has("trans") + Agent.matches(of(Noun)) + Patient.matches(of(PersonalPronoun))
                to RemapOrderTransformer(mapOf(Agent to Patient, Patient to Agent)))
            .takeIf { 0.05.testProbability() }
        )
}
