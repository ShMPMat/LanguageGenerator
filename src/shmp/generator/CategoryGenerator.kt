
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
        randomArticles(),
        randomGender(),
        randomNumber(),
        randomTense()
    )

    private fun randomPerson(): Pair<Person, CategoryRandomSupplements> {
        val presentElements = randomElement(
            PersonPresence.values(),
            random
        ).possibilities
        val affectedSpeechParts = randomAffectedSpeechParts(PersonRandomSupplements)
        return Person(presentElements, affectedSpeechParts) to PersonRandomSupplements
    }

    private fun randomArticles(): Pair<Articles, CategoryRandomSupplements> {
        val presentElements = randomElement(
            ArticlePresence.values(),
            random
        ).presentArticles
        val affectedSpeechParts = randomAffectedSpeechParts(ArticlesRandomSupplements)
        return Articles(presentElements, affectedSpeechParts) to ArticlesRandomSupplements
    }

    private fun randomGender(): Pair<Gender, CategoryRandomSupplements> {
        val type = randomElement(
            GenderPresence.values(),
            random
        )
        val presentElements = if (type == GenderPresence.NonGendered)
            randomSublist(type.possibilities, random, min = 2)
        else
            type.possibilities
        val affectedSpeechParts = randomAffectedSpeechParts(GenderRandomSupplements)
        return Gender(presentElements, affectedSpeechParts) to GenderRandomSupplements
    }

    private fun randomNumber(): Pair<Numbers, CategoryRandomSupplements> {
        val presentElements = randomElement(
            NumbersPresence.values(),
            random
        ).possibilities
        val affectedSpeechParts = randomAffectedSpeechParts(NumbersRandomSupplements)
        return Numbers(presentElements, affectedSpeechParts) to NumbersRandomSupplements
    }

    private fun randomTense(): Pair<Tense, CategoryRandomSupplements> {
        val presentElements = randomElement(
            TensePresence.values(),
            random
        ).possibilities
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