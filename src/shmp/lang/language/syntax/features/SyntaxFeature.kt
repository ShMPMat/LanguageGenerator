package shmp.lang.language.syntax.features

import shmp.random.SampleSpaceObject
import shmp.random.UnwrappableSSO


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
