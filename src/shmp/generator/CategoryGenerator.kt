
package shmp.generator

import shmp.language.CategoryRealization
import shmp.language.SpeechPart
import shmp.language.categories.*
import shmp.random.randomElement
import shmp.random.randomSublist
import kotlin.random.Random

class CategoryGenerator(
    private val random: Random
) {
    internal fun randomCategories() = listOf(
        randomArticles(),
        randomGender(),
        randomNumber(),
        randomTense()
    )

    private fun randomArticles(): Pair<Articles, RealizationMapper> {
        val presentElements = randomElement(
            ArticlePresence.values(),
            random
        ).presentArticles
        val affectedSpeechParts = randomAffectedSpeechParts(ArticlesRandomSupplements)
        return Articles(presentElements, affectedSpeechParts) to ArticlesRandomSupplements::realizationTypeProbability
    }

    private fun randomGender(): Pair<Gender, RealizationMapper> {
        val type = randomElement(
            GenderPresence.values(),
            random
        )
        val presentElements = if (type == GenderPresence.NonGendered)
            randomSublist(type.possibilities, random, min = 2)
        else
            type.possibilities
        val affectedSpeechParts = randomAffectedSpeechParts(GenderRandomSupplements)
        return Gender(presentElements, affectedSpeechParts) to GenderRandomSupplements::realizationTypeProbability
    }

    private fun randomNumber(): Pair<Numbers, RealizationMapper> {
        val presentElements = randomElement(
            NumbersPresence.values(),
            random
        ).possibilities
        val affectedSpeechParts = randomAffectedSpeechParts(NumbersRandomSupplements)
        return Numbers(presentElements, affectedSpeechParts) to NumbersRandomSupplements::realizationTypeProbability
    }

    private fun randomTense(): Pair<Tense, RealizationMapper> {
        val presentElements = randomElement(
            TensePresence.values(),
            random
        ).possibilities
        val affectedSpeechParts = randomAffectedSpeechParts(TenseRandomSupplements)
        return Tense(presentElements, affectedSpeechParts) to TenseRandomSupplements::realizationTypeProbability
    }

    private fun randomAffectedSpeechParts(categoryRandomSupplements: CategoryRandomSupplements<*>): Set<SpeechPart> =
        setOf(categoryRandomSupplements.mainSpeechPart).union(
            randomSublist(
                SpeechPart.values(),
                categoryRandomSupplements::speechPartProbabilities,
                random
            )
        )
}

typealias RealizationMapper = (CategoryRealization) -> Double