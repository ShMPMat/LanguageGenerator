
package shmp.generator

import shmp.language.SpeechPart
import shmp.language.categories.*
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.random.Random

class CategoryGenerator(
    private val random: Random
) {
    internal fun randomCategories() = listOf(
        randomPerson(),
        randomDefiniteness(),
        randomGender(),
        randomNumber(),
        randomTense()
    )

    private fun randomPerson(): Pair<Person, CategoryRandomSupplements> {
        val presentElements = PersonRandomSupplements.randomRealization(random)
        val affectedSpeechParts = randomAffectedSpeechParts(PersonRandomSupplements)
        return Person(presentElements, affectedSpeechParts) to PersonRandomSupplements
    }

    private fun randomDefiniteness(): Pair<Definiteness, CategoryRandomSupplements> {
        val presentElements = DefinitenessRandomSupplements.randomRealization(random)
        val affectedSpeechParts = randomAffectedSpeechParts(DefinitenessRandomSupplements)
        return Definiteness(presentElements, affectedSpeechParts) to DefinitenessRandomSupplements
    }

    private fun randomGender(): Pair<Gender, CategoryRandomSupplements> {
        val presentElements = GenderRandomSupplements.randomRealization(random)
        val affectedSpeechParts = randomAffectedSpeechParts(GenderRandomSupplements)
        return Gender(presentElements, affectedSpeechParts) to GenderRandomSupplements
    }

    private fun randomNumber(): Pair<Numbers, CategoryRandomSupplements> {
        val presentElements = NumbersRandomSupplements.randomRealization(random)
        val affectedSpeechParts = randomAffectedSpeechParts(NumbersRandomSupplements)
        return Numbers(presentElements, affectedSpeechParts) to NumbersRandomSupplements
    }

    private fun randomTense(): Pair<Tense, CategoryRandomSupplements> {
        val presentElements = TenseRandomSupplements.randomRealization(random)
        val affectedSpeechParts = randomAffectedSpeechParts(TenseRandomSupplements)
        return Tense(presentElements, affectedSpeechParts) to TenseRandomSupplements
    }

    private fun randomAffectedSpeechParts(supplements: CategoryRandomSupplements): Set<SpeechPart> {
        val max = SpeechPart.values().map { supplements.speechPartProbabilities(it) }.max()
            ?: throw GeneratorException("No SpeechPart exists")
        return SpeechPart.values().mapNotNull {
                val probability = supplements.speechPartProbabilities(it) / max
                if (testProbability(probability, random)) it else null
            }.toSet()
    }
}