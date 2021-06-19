package shmp.lang.generator

import shmp.lang.generator.util.DataConsistencyException
import shmp.lang.language.AbstractCategoryValue
import shmp.lang.language.category.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.lexis.SpeechPart
import shmp.random.singleton.chanceOf


class CategoryGenerator {
    internal fun randomCategories(): List<Pair<Category, CategoryRandomSupplements>> {
        val defaults = mutableListOf(
            randomCategory({ l: List<PersonValue>, s, ss -> Person(l, s, ss) }, PersonRandomSupplements),
            randomCategory({ l: List<DefinitenessValue>, s, ss -> Definiteness(l, s, ss) }, DefinitenessRandomSupplements),
            randomCategory({ l: List<NounClassValue>, s, ss -> NounClass(l, s, ss) }, NounClassRandomSupplements),
            randomCategory({ l: List<AnimosityValue>, s, ss -> Animosity(l, s, ss) }, AnimosityRandomSupplements),
            randomCategory({ l: List<NumbersValue>, s, ss -> Numbers(l, s, ss) }, NumbersRandomSupplements),
            randomCategory({ l: List<TenseValue>, s, ss -> Tense(l, s, ss) }, TenseRandomSupplements),
            randomCategory({ l: List<NegationValue>, s, ss -> Negation(l, s, ss) }, NegationRandomSupplements),
            randomCategory({ l: List<DeixisValue>, s, ss -> Deixis(l, s, ss) }, DeixisRandomSupplements)
        )

        val caseCategory = randomCategory(
            { l: List<CaseValue>, s, ss -> Case(l, s, ss) },
            CaseRandomSupplements
        )
        defaults.add(caseCategory)

        val absentScenarios = caseCategory.first.allPossibleValues
            .filter { it !in caseCategory.first.actualValues && it in nonCoreCases }

        if (absentScenarios.isNotEmpty()) {
            val values = absentScenarios
                .map { AbstractCategoryValue(it.semanticsCore, adpositionName, it.shortName) }
            val allPossibleValues = caseCategory.first.allPossibleValues
                .map { AbstractCategoryValue(it.semanticsCore, adpositionName, it.shortName) }
                .toSet()

            val affectedSpeechPartsAndSources = randomAffectedSpeechParts(AdpositionRandomSupplements)

            val adpositionCategory = AbstractChangeCategory(
                values,
                allPossibleValues,
                affectedSpeechPartsAndSources,
                setOf(),
                adpositionName
            )

            defaults.add(adpositionCategory to AdpositionRandomSupplements)
        }

        return defaults
    }

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

                probability.chanceOf<SourceTemplate> { it }
            }.map { PSpeechPart(speechPart, it.source) }
        }.toSet()
    }
}
