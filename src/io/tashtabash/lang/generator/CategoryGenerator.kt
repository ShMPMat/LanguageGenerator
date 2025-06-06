package io.tashtabash.lang.generator

import io.tashtabash.lang.generator.util.DataConsistencyException
import io.tashtabash.lang.language.category.value.AbstractCategoryValue
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.random.singleton.chanceOf


class CategoryGenerator {
    internal fun randomCategories(): List<SupplementedCategory> {
        val defaults = mutableListOf(
            randomCategory({ l: List<PersonValue>, s, ss -> Person(l, s, ss) }, PersonRandomSupplements),
            randomCategory({ l: List<InclusivityValue>, s, ss -> Inclusivity(l, s, ss) }, InclusivityRandomSupplements),
            randomCategory({ l: List<DefinitenessValue>, s, ss -> Definiteness(l, s, ss) }, DefinitenessRandomSupplements),
            randomCategory({ l: List<NounClassValue>, s, ss -> NounClass(l, s, ss) }, NounClassRandomSupplements()),
            randomCategory({ l: List<AnimosityValue>, s, ss -> Animosity(l, s, ss) }, AnimosityRandomSupplements),
            randomCategory({ l: List<NumberValue>, s, ss -> Number(l, s, ss) }, NumberRandomSupplements),
            randomCategory({ l: List<TenseValue>, s, ss -> Tense(l, s, ss) }, TenseRandomSupplements),
            randomCategory({ l: List<NegationValue>, s, ss -> Negation(l, s, ss) }, NegationRandomSupplements),
            randomCategory({ l: List<DeixisValue>, s, ss -> Deixis(l, s, ss) }, DeixisRandomSupplements)
        )

        val caseCategory = randomCategory(
            { l: List<CaseValue>, s, ss -> Case(l, s, ss) },
            CaseRandomSupplements()
        )
        defaults += caseCategory

        val absentScenarios = caseCategory.first.allPossibleValues
            .filter { it !in caseCategory.first.actualValues && it in nonCoreCases }

        if (absentScenarios.isNotEmpty()) {
            val values = absentScenarios
                .map { AbstractCategoryValue(it.semanticsCore, adpositionName, it.alias) }
            val allPossibleValues = caseCategory.first.allPossibleValues
                .map { AbstractCategoryValue(it.semanticsCore, adpositionName, it.alias) }
                .toSet()

            val affectedSpeechPartsAndSources = randomAffectedSpeechParts(AdpositionRandomSupplements)

            val adpositionCategory = AbstractChangeCategory(
                values,
                allPossibleValues,
                affectedSpeechPartsAndSources,
                setOf(),
                adpositionName
            )

            defaults += adpositionCategory to AdpositionRandomSupplements
        }

        return defaults
    }

    private fun <E: CategoryValue> randomCategory(
        constructor: (List<E>, Set<PSpeechPart>, Set<SpeechPart>) -> Category,
        supplements: CategoryRandomSupplements
    ): SupplementedCategory {
        val presentElements = supplements.randomRealization()
        val affectedSpeechPartsAndSources = randomAffectedSpeechParts(supplements)
        val affectedSpeechParts = affectedSpeechPartsAndSources.map { it.speechPart }
        val staticSpeechParts = supplements.randomStaticSpeechParts()
            .filter { it in affectedSpeechParts }
            .toSet()

        try {
            return constructor(presentElements as List<E>, affectedSpeechPartsAndSources, staticSpeechParts) to supplements
        } catch (e: Exception) {
            throw DataConsistencyException("Wrong supplements with name ${supplements.javaClass.name}")
        }
    }

    private fun randomAffectedSpeechParts(supplements: CategoryRandomSupplements): Set<PSpeechPart> {
        val max = SpeechPart.entries
            .flatMap { supplements.speechPartProbabilities(it) }
            .maxOfOrNull { it.probability }
            ?: throw DataConsistencyException("No SpeechPart exists")

        return SpeechPart.entries.flatMap { speechPart ->
            supplements.speechPartProbabilities(speechPart).mapNotNull {
                val probability = it.probability / max

                probability.chanceOf<SourceTemplate> { it }
            }.map { PSpeechPart(speechPart, it.source) }
        }.toSet()
    }
}

typealias SupplementedCategory = Pair<Category, CategoryRandomSupplements>
typealias SupplementedSourcedCategory = Pair<SourcedCategory, CategoryRandomSupplements>
