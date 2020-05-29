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
        randomCategory({ l: List<PersonValue>, s, ss -> Person(l, s, ss) }, PersonRandomSupplements),
        randomCategory({ l: List<DefinitenessValue>, s, ss -> Definiteness(l, s, ss) }, DefinitenessRandomSupplements),
        randomCategory({ l: List<GenderValue>, s, ss -> Gender(l, s, ss) }, GenderRandomSupplements),
        randomCategory({ l: List<AnimosityValue>, s, ss -> Animosity(l, s, ss) }, AnimosityRandomSupplements),
        randomCategory({ l: List<NumbersValue>, s, ss -> Numbers(l, s, ss) }, NumbersRandomSupplements),
        randomCategory({ l: List<TenseValue>, s, ss -> Tense(l, s, ss) }, TenseRandomSupplements)
    )

    private fun <E: CategoryValue> randomCategory(
        constructor: (List<E>, Set<SpeechPart>, Set<SpeechPart>) -> AbstractChangeCategory,//TODO to Category?
        supplements: CategoryRandomSupplements
    ): Pair<AbstractChangeCategory, CategoryRandomSupplements> {
        val presentElements = supplements.randomRealization(random)
        val affectedSpeechParts = randomAffectedSpeechParts(supplements)
        val staticSpeechParts = supplements.randomStaticSpeechParts(random)
            .filter { it in affectedSpeechParts }
            .toSet()
        return try {
            constructor(presentElements as List<E>, affectedSpeechParts, staticSpeechParts) to supplements
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