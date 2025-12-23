package io.tashtabash.lang.generator

import io.tashtabash.lang.generator.util.DataConsistencyException
import io.tashtabash.lang.language.category.value.AbstractCategoryValue
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.random.singleton.chanceOf


class CategoryGenerator {
    internal fun randomCategories(): List<SupplementedCategory<*>> {
        val defaults = mutableListOf(
            randomCategory({ l, s, ss -> Person(l, s, ss) }, PersonRandomSupplements),
            randomCategory({ l, s, ss -> Inclusivity(l, s, ss) }, InclusivityRandomSupplements),
            randomCategory({ l, s, ss -> Definiteness(l, s, ss) }, DefinitenessRandomSupplements),
            randomCategory({ l, s, ss -> NounClass(l, s, ss) }, NounClassRandomSupplements),
            randomCategory({ l, s, ss -> Animosity(l, s, ss) }, AnimosityRandomSupplements),
            randomCategory({ l, s, ss -> Number(l, s, ss) }, NumberRandomSupplements),
            randomCategory({ l, s, ss -> Tense(l, s, ss) }, TenseRandomSupplements),
            randomCategory({ l, s, ss -> Negation(l, s, ss) }, NegationRandomSupplements),
            randomCategory({ l, s, ss -> Deixis(l, s, ss) }, DeixisRandomSupplements)
        )

        val caseCategory = randomCategory({ l, s, ss -> Case(l, s, ss) }, CaseRandomSupplements())
        defaults += caseCategory

        generateAdpositions(caseCategory)?.let {
            defaults += it
        }

        return defaults
    }

    private fun generateAdpositions(
        caseCategory: SupplementedCategory<CaseValue>,
    ): Pair<AbstractChangeCategory, AdpositionRandomSupplements>? {
        val absentScenarios = caseCategory.first.allPossibleValues
            .filter { it !in caseCategory.first.actualValues && it in nonCoreCases }

        if (absentScenarios.isEmpty())
            return null

        val values = absentScenarios.map { AbstractCategoryValue(it.semanticsCore, adpositionName, it.alias) }
        val allPossibleValues = caseCategory.first
            .allPossibleValues
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

        return adpositionCategory to AdpositionRandomSupplements
    }

    private fun <E: CategoryValue> randomCategory(
        constructor: (List<E>, Set<PSpeechPart>, Set<SpeechPart>) -> Category,
        supplements: CategoryRandomSupplements<E>
    ): SupplementedCategory<E> {
        val presentElements = supplements.randomRealization()
        val affectedSpeechPartsAndSources = randomAffectedSpeechParts(supplements)
        val affectedSpeechParts = affectedSpeechPartsAndSources.map { it.speechPart }
        val staticSpeechParts = supplements.randomStaticSpeechParts()
            .filter { it in affectedSpeechParts }
            .toSet()

        return constructor(presentElements, affectedSpeechPartsAndSources, staticSpeechParts) to supplements
    }

    private fun randomAffectedSpeechParts(supplements: CategoryRandomSupplements<*>): Set<PSpeechPart> {
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

typealias SupplementedCategory<E> = Pair<Category, CategoryRandomSupplements<E>>
typealias SupplementedSourcedCategory<E> = Pair<SourcedCategory, CategoryRandomSupplements<E>>
