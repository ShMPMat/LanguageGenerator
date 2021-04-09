package shmp.lang.generator

import shmp.lang.generator.util.DataConsistencyException
import shmp.lang.language.category.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.lexis.SpeechPart
import shmp.random.testProbability
import kotlin.random.Random


class CategoryGenerator(
    private val random: Random
) {
    internal fun randomCategories() = listOf(
        randomCategory({ l: List<PersonValue>, s, ss -> Person(l, s, ss) }, PersonRandomSupplements),
        randomCategory({ l: List<DefinitenessValue>, s, ss -> Definiteness(l, s, ss) }, DefinitenessRandomSupplements),
        randomCategory({ l: List<NounClassValue>, s, ss -> NounClass(l, s, ss) }, NounClassRandomSupplements),
        randomCategory({ l: List<AnimosityValue>, s, ss -> Animosity(l, s, ss) }, AnimosityRandomSupplements),
        randomCategory({ l: List<NumbersValue>, s, ss -> Numbers(l, s, ss) }, NumbersRandomSupplements),
        randomCategory({ l: List<TenseValue>, s, ss -> Tense(l, s, ss) }, TenseRandomSupplements),
        randomCategory({ l: List<DeixisValue>, s, ss -> Deixis(l, s, ss) }, DeixisRandomSupplements),
        randomCategory({ l: List<CaseValue>, s, ss -> Case(l, s, ss) }, CaseRandomSupplements)
    )

    private fun <E: CategoryValue> randomCategory(
        constructor: (List<E>, Set<PSpeechPart>, Set<SpeechPart>) -> Category,
        supplements: CategoryRandomSupplements
    ): Pair<Category, CategoryRandomSupplements> {
        val presentElements = supplements.randomRealization()
        val affectedSpeechPartsAndSources = randomAffectedSpeechParts(supplements)
        val affectedSpeechParts = affectedSpeechPartsAndSources.map { it.speechPart }
        val staticSpeechParts = supplements.randomStaticSpeechParts()
            .filter { it in affectedSpeechParts }
            .toSet()
        return try {
            constructor(presentElements as List<E>, affectedSpeechPartsAndSources, staticSpeechParts) to supplements
        } catch (e: Exception) {
            throw DataConsistencyException("Wrong supplements with name ${supplements.javaClass.name}")
        }
    }

    private fun randomAffectedSpeechParts(supplements: CategoryRandomSupplements): Set<PSpeechPart> {
        val max = SpeechPart.values()
            .flatMap { supplements.speechPartProbabilities(it) }
            .map { it.probability }
            .maxOrNull()
            ?: throw DataConsistencyException("No SpeechPart exists")

        return SpeechPart.values().flatMap { speechPart ->
            supplements.speechPartProbabilities(speechPart).mapNotNull {
                val probability = it.probability / max
                if (testProbability(probability, random)) it else null
            }.map { PSpeechPart(speechPart, it.source) }
        }.toSet()
    }
}
