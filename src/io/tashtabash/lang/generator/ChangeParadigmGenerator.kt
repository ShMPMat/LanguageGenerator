package io.tashtabash.lang.generator

import io.tashtabash.lang.generator.util.copyForNewSpeechPart
import io.tashtabash.lang.generator.util.substituteWith
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.CategorySource.Agreement
import io.tashtabash.lang.language.category.paradigm.CompulsoryData
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.category.realization.WordCategoryApplicator
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.lexis.SpeechPart.Article
import io.tashtabash.lang.language.lexis.SpeechPart.Verb
import io.tashtabash.lang.language.phonology.PhoneticRestrictions
import io.tashtabash.lang.language.phonology.RestrictionsParadigm
import io.tashtabash.lang.language.phonology.prosody.ProsodyChangeParadigm
import io.tashtabash.lang.language.phonology.prosody.StressType
import io.tashtabash.lang.language.syntax.ChangeParadigm
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import kotlin.math.max


class ChangeParadigmGenerator(
    private val stressPattern: StressType,
    lexisGenerator: LexisGenerator,
    changeGenerator: ChangeGenerator,
    private val restrictionsParadigm: RestrictionsParadigm
) {
    private val applicatorsGenerator = ApplicatorsGenerator(lexisGenerator, changeGenerator)
    private val wordOrderGenerator = WordOrderGenerator()
    private val syntaxParadigmGenerator = SyntaxParadigmGenerator()
    val numeralParadigmGenerator = NumeralParadigmGenerator()

    private val emptyArticleParadigm = SpeechPartChangeParadigm(Article.toDefault())

    internal fun generateChangeParadigm(categoriesWithMappers: List<SupplementedCategory>): ChangeParadigm {
        val categories = categoriesWithMappers.map { it.first }

        val oldCategoryData = mutableListOf<SpeechPartCategoryData>()
        val oldSpeechParts = mutableListOf<TypedSpeechPart>()
        val speechParts = SpeechPart.entries.map { it.toDefault() }.toMutableList()
        val newSpeechParts = mutableSetOf<TypedSpeechPart>()

        val speechPartChangesMap = mutableMapOf<TypedSpeechPart, SpeechPartChangeParadigm>()

        while (speechParts.isNotEmpty()) {
            val categoryData = generateCategoryData(speechParts, categoriesWithMappers)

            oldSpeechParts += speechParts
            oldCategoryData += categoryData

            checkCompulsoryConsistency(categoryData, oldCategoryData)

            categoryData.map { (speechPart, restrictions, categoriesAndSupply) ->
                val (words, applicators, orderedClusters) = applicatorsGenerator.randomApplicatorsForSpeechPart(
                    speechPart,
                    restrictions,
                    categoriesAndSupply
                )
                for (word in words)
                    if (word.semanticsCore.speechPart !in oldSpeechParts)
                        newSpeechParts += word.semanticsCore.speechPart

                val changeParadigm = SpeechPartChangeParadigm(
                    speechPart,
                    orderedClusters,
                    applicators,
                    ProsodyChangeParadigm(stressPattern)
                )
                speechPartChangesMap[speechPart] = changeParadigm

                restrictionsParadigm.restrictionsMapper[speechPart] = restrictions.copy(
                    avgWordLength = max(2, restrictions.avgWordLength - changeParadigm.exponenceClusters.size)
                )
            }
            println(newSpeechParts)
            speechParts.clear()
            speechParts += newSpeechParts
            newSpeechParts.clear()
        }

        val verbParadigm = speechPartChangesMap.getValue(Verb.toDefault())
        val verbRestrictions = restrictionsParadigm.restrictionsMapper.getValue(Verb.toDefault())
        speechPartChangesMap[Verb.toIntransitive()] = generateIntransitiveVerbs(verbParadigm)
        restrictionsParadigm.restrictionsMapper[Verb.toIntransitive()] = verbRestrictions.copy()

        simplifyParadigm(speechPartChangesMap)

        if (!articlePresent(categories, speechPartChangesMap))
            speechPartChangesMap[Article.toDefault()] = emptyArticleParadigm

        val wordChangeParadigm = WordChangeParadigm(categories, speechPartChangesMap)
        val syntaxParadigm = syntaxParadigmGenerator.generateSyntaxParadigm(wordChangeParadigm)
        val wordOrder = wordOrderGenerator.generateWordOrder(syntaxParadigm)
        val syntaxLogic = SyntaxLogicGenerator(wordChangeParadigm, syntaxParadigm).generateSyntaxLogic()
        val numeralParadigm = numeralParadigmGenerator.generateNumeralParadigm()

        return ChangeParadigm(wordOrder, wordChangeParadigm, syntaxParadigm, numeralParadigm, syntaxLogic)
    }

    private fun generateCategoryData(
        speechParts: List<TypedSpeechPart>,
        categoriesWithMappers: List<SupplementedCategory>
    ) = speechParts.map { speechPart ->
        val restrictions = restrictionsParadigm.restrictionsMapper.getValue(speechPart)
        val categoriesAndSupply = generateSpeechPartCategories(
            speechPart,
            categoriesWithMappers
        )

        SpeechPartCategoryData(speechPart, restrictions, categoriesAndSupply)
    }

    private fun generateSpeechPartCategories(
        speechPart: TypedSpeechPart,
        categoriesWithMappers: List<SupplementedCategory>
    ): List<Pair<SourcedCategory, CategoryRandomSupplements>> {
        val presentCategories = categoriesWithMappers
            .filter { it.first.speechParts.contains(speechPart.type) }
            .filter { it.first.actualValues.isNotEmpty() }

        return presentCategories.flatMap { (c, s) ->
            c.affected.filter { it.speechPart == speechPart.type }
                .map {
                    var compulsoryData = s.randomIsCompulsory(speechPart.type)
                    if (c.actualValues.size <= 1)
                        compulsoryData = compulsoryData.copy(isCompulsory = false)

                    val existingCoCategories = compulsoryData.compulsoryCoCategories.filter {
                        presentCategories.any { sc -> sc.first.outType == it.first().parentClassName }
                    }
                    compulsoryData = compulsoryData.copy(compulsoryCoCategories = existingCoCategories)

                    val source = if (speechPart.subtype == adnominalSubtype /*&& c.outType != deixisName*/)
                        Agreement(Agent, nominals)
                    else it.source

                    SourcedCategory(c, source, compulsoryData) to s
                }
        }
    }

    private fun generateIntransitiveVerbs(verbParadigm: SpeechPartChangeParadigm) =
        verbParadigm.copyForNewSpeechPart(Verb.toIntransitive(), mapOf(Agent to Argument)) { c ->
            c.categories.none { it.source is Agreement && it.source.relation == Patient }
        }

    // Fixes edge cases when a speech part with a compulsory category agrees with a speech part in which
    // this category isn't compulsory
    private fun checkCompulsoryConsistency(
        targetCategoryData: List<SpeechPartCategoryData>,
        allCategoryData: MutableList<SpeechPartCategoryData>
    ) {
        var shouldCheck = true
        while (shouldCheck) {
            shouldCheck = false

            for (data in targetCategoryData)
                for (sourcedCategory in data.categories) {
                    if (sourcedCategory.category.outType in compulsoryConsistencyExceptions)
                        continue
                    if (sourcedCategory.source !is Agreement || !sourcedCategory.compulsoryData.isCompulsory)
                        continue

                    val relevantCategories = sourcedCategory.source.possibleSpeechParts
                        .flatMap { sp -> allCategoryData.filter { it.speechPart.type == sp } }
                        .map { d -> d.categories.firstOrNull { it.category == sourcedCategory.category } }
                    val areAllRelationsCompulsory = relevantCategories.all { c ->
                        c?.compulsoryData?.isCompulsory ?: false
                    }
                    val allCoCategories = relevantCategories
                        .mapNotNull { it?.compulsoryData?.compulsoryCoCategories }
                        .flatten()

                    val newCompulsoryData = CompulsoryData(areAllRelationsCompulsory, allCoCategories)

                    if (newCompulsoryData != sourcedCategory.compulsoryData)
                        shouldCheck = true

                    sourcedCategory.compulsoryData = newCompulsoryData
                }
        }
    }

    private fun simplifyParadigm(speechPartChangesMap: MutableMap<TypedSpeechPart, SpeechPartChangeParadigm>) {
        for ((speechParts, probability) in sameParadigmList)
            probability.chanceOf {
                val (from, to) = speechParts
                val fromParadigm = speechPartChangesMap.map { it.value }
                    .filter { it.speechPart.type == from }
                    .randomElement()
                val toParadigms = speechPartChangesMap.map { it.value }
                    .filter { it.speechPart.type == to }

                for (toParadigm in toParadigms)
                    speechPartChangesMap[toParadigm.speechPart] = toParadigm.substituteWith(fromParadigm)
            }
    }

    private fun articlePresent(
        categories: List<Category>,
        speechPartChangesMap: MutableMap<TypedSpeechPart, SpeechPartChangeParadigm>
    ) = if (categories.first { it.outType == definitenessName }.actualValues.isNotEmpty())
        speechPartChangesMap.any { (_, u) ->
            u.applicators.values
                .flatMap { it.values }
                .any { it is WordCategoryApplicator && it.word.semanticsCore.speechPart.type == Article }
        }
    else false

    private val compulsoryConsistencyExceptions = listOf(inclusivityName)
}


private data class SpeechPartCategoryData(
    val speechPart: TypedSpeechPart,
    val restrictionsParadigm: PhoneticRestrictions,
    val categoriesAndSupply: List<Pair<SourcedCategory, CategoryRandomSupplements>>
) {
    val categories = categoriesAndSupply.map { it.first }
}
