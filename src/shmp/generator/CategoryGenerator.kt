package shmp.generator

import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.language.category.*
import shmp.random.testProbability
import kotlin.random.Random

class CategoryGenerator(
    private val random: Random
) {
    internal fun randomCategories() = listOf(
        randomCategory({ l: List<PersonValue>, s -> Person(l, s) }, PersonRandomSupplements),
        randomCategory({ l: List<DefinitenessValue>, s -> Definiteness(l, s) }, DefinitenessRandomSupplements),
        randomCategory({ l: List<GenderValue>, s -> Gender(l, s) }, GenderRandomSupplements),
        randomCategory({ l: List<NumbersValue>, s -> Numbers(l, s) }, NumbersRandomSupplements),
        randomCategory({ l: List<TenseValue>, s -> Tense(l, s) }, TenseRandomSupplements)
    )

    private fun <E: CategoryValue> randomCategory(
        constructor: (List<E>, Set<SpeechPart>) -> AbstractChangeCategory,//TODO to Category?
        supplements: CategoryRandomSupplements
    ): Pair<AbstractChangeCategory, CategoryRandomSupplements> {
        val presentElements = supplements.randomRealization(random)
        val affectedSpeechParts = randomAffectedSpeechParts(supplements)
        return try {
            constructor(presentElements as List<E>, affectedSpeechParts) to supplements
        } catch (e: Exception) {
            throw GeneratorException("Wrong supplements with name ${supplements.javaClass.name}")
        }
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