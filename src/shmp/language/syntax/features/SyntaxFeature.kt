package shmp.language.syntax.features

import shmp.random.SampleSpaceObject
import shmp.random.UnwrappableSSO


interface SyntaxFeature: SampleSpaceObject


class ParametrizedSyntaxFeature<E: SyntaxFeature>(
    val feature: E,
    override val probability: Double
): UnwrappableSSO<E>(feature)

fun <E: SyntaxFeature> E.parametrize(probability: Double) =
    ParametrizedSyntaxFeature(this, probability)


typealias ParametrizedSyntaxFeatureList<E> = List<ParametrizedSyntaxFeature<E>>
