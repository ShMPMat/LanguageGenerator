package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.category.caseName
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.syntax.SyntaxParadigm
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction.*
import io.tashtabash.lang.language.syntax.clause.construction.PredicatePossessionConstruction.*
import io.tashtabash.lang.language.syntax.features.CopulaPresence
import io.tashtabash.lang.language.syntax.features.PredicatePossessionPresence
import io.tashtabash.random.UnwrappableSSO
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomUnwrappedElement
import io.tashtabash.random.withProb


class SyntaxParadigmGenerator {
    internal fun generateSyntaxParadigm(wordChangeParadigm: WordChangeParadigm) = SyntaxParadigm(
        generateCopula(),
        generatePossession(wordChangeParadigm),
    )

    private fun generateCopula(): CopulaPresence {
        val mainCopulaType = copulaProbabilities.randomElement()
        val noneProbability = RandomSingleton.random.nextDouble(mainCopulaType.probability)
            .let {
                if (it <= mainCopulaType.probability / 5) .0
                else it
            }
        return CopulaPresence(
            listOf(mainCopulaType) +
                    if (noneProbability != .0)
                        listOf(None.withProb(noneProbability))
                    else listOf()
        )
    }

    private fun generatePossession(wordChangeParadigm: WordChangeParadigm): PredicatePossessionPresence {
        val possiblePossessionType = predicatePossessionProbabilities.toMutableList()
        if (CaseValue.Topic !in wordChangeParadigm[caseName].actualValues)
            possiblePossessionType.removeIf { it.value == Topic }
        val possessionConstructionPresence = PredicatePossessionPresence(
            listOf(possiblePossessionType.randomUnwrappedElement().withProb(1.0))
        )
        return possessionConstructionPresence
    }
}


val predicatePossessionProbabilities = listOf(
    HaveVerb.withProb(63.0),
    LocativeOblique.withProb(24.0),
    DativeOblique.withProb(24.0),
    GenitiveOblique.withProb(22.0),
    Topic.withProb(100.0),
//    Conjunctional(59.0)
)

val copulaProbabilities = listOf<UnwrappableSSO<CopulaConstruction>>(
    Verb.withProb(1.0),
    Particle.withProb(.25),
    None.withProb(.1)
)
