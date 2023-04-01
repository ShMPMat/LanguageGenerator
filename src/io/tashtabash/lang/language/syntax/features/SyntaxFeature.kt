package io.tashtabash.lang.language.syntax.features

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.UnwrappableSSO


interface SyntaxFeature: SampleSpaceObject


class SsoSyntaxFeature<E: SyntaxFeature>(
    val feature: E,
    override val probability: Double
): UnwrappableSSO<E>(feature) {
    override fun toString() = "$feature with chance $probability"
}

fun <E: SyntaxFeature> E.toSso(probability: Double) =
    SsoSyntaxFeature(this, probability)


typealias SsoSyntaxFeatures<E> = List<SsoSyntaxFeature<E>>
