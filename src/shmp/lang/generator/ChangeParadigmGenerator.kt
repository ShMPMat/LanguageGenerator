package shmp.lang.generator

import shmp.lang.generator.util.copyForNewSpeechPart
import shmp.lang.generator.util.substituteWith
import shmp.lang.language.category.Category
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.CategorySource.RelationGranted
import shmp.lang.language.category.definitenessName
import shmp.lang.language.category.inclusivityOutName
import shmp.lang.language.category.paradigm.CompulsoryData
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.SpeechPartChangeParadigm
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.category.realization.WordCategoryApplicator
import shmp.lang.language.lexis.*
import shmp.lang.language.lexis.SpeechPart.Article
import shmp.lang.language.lexis.SpeechPart.Verb
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.prosody.ProsodyChangeParadigm
import shmp.lang.language.phonology.prosody.StressType
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.random.singleton.chanceOf
import shmp.random.singleton.chanceOfNot
import shmp.random.singleton.randomElement
import kotlin.math.max


class ChangeParadigmGenerator(
    private val stressPattern: StressType,
    lexisGenerator: LexisGenerator,
    changeGenerator: ChangeGenerator,
    private val restrictionsParadigm: RestrictionsParadigm
) {
    private val speechPartApplicatorsGenerator = SpeechPartApplicatorsGenerator(lexisGenerator, changeGenerator)
    private val wordOrderGenerator = WordOrderGenerator()
    private val syntaxParadigmGenerator = SyntaxParadigmGenerator()

    internal fun generateChangeParadigm(categoriesWithMappers: List<SupplementedCategory>): ChangeParadigm {
        val categories = categoriesWithMappers.map { it.first }

        val oldSpeechParts = mutableListOf<TypedSpeechPart>()
        val speechParts = SpeechPart.values().map { it.toUnspecified() }.toMutableList()
        val newSpeechParts = mutableSetOf<TypedSpeechPart>()

        val speechPartChangesMap = mutableMapOf<TypedSpeechPart, SpeechPartChangeParadigm>()

        while (speechParts.isNotEmpty()) {
            oldSpeechParts.addAll(speechParts)
            speechParts.map { speechPart ->
                val restrictions = restrictionsParadigm.restrictionsMapper.getValue(speechPart)
                val categoriesAndSupply = generateSpeechPartCategories(
                    speechPart,
                    categoriesWithMappers
                )

                val (words, applicators) = speechPartApplicatorsGenerator
                    .randomApplicatorsForSpeechPart(
                        speechPart,
                        restrictions,
                        categoriesAndSupply
                    )
                for (word in words)
                    if (word.semanticsCore.speechPart !in oldSpeechParts)
                        newSpeechParts.add(word.semanticsCore.speechPart)

                val orderedClusters = speechPartApplicatorsGenerator.randomApplicatorsOrder(applicators)
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
            speechParts.addAll(newSpeechParts)
            newSpeechParts.clear()
        }

        val verbParadigm = speechPartChangesMap.getValue(Verb.toUnspecified())
        val verbRestrictions = restrictionsParadigm.restrictionsMapper.getValue(Verb.toUnspecified())
        speechPartChangesMap[Verb.toIntransitive()] = generateIntransitiveVerbs(verbParadigm)
        restrictionsParadigm.restrictionsMapper[Verb.toIntransitive()] = verbRestrictions.copy()

        checkCompulsoryConsistency(speechPartChangesMap)//TODO it will break cluster necessity, should move it up
        simplifyParadigm(speechPartChangesMap)

        if (!articlePresent(categories, speechPartChangesMap))
            speechPartChangesMap[Article.toUnspecified()] = emptyArticleParadigm

        val wordChangeParadigm = WordChangeParadigm(categories, speechPartChangesMap)
        val syntaxParadigm = syntaxParadigmGenerator.generateSyntaxParadigm(wordChangeParadigm)
        val wordOrder = wordOrderGenerator.generateWordOrder(syntaxParadigm)
        val syntaxLogic = SyntaxLogicGenerator(wordChangeParadigm, syntaxParadigm).generateSyntaxLogic()

        return ChangeParadigm(wordOrder, wordChangeParadigm, syntaxParadigm, syntaxLogic)
    }

    private val emptyArticleParadigm = SpeechPartChangeParadigm(
        Article.toUnspecified(),
        listOf(),
        mapOf(),
        ProsodyChangeParadigm(StressType.None)
    )

    private fun generateSpeechPartCategories(
        speechPart: TypedSpeechPart,
        categoriesWithMappers: List<SupplementedCategory>
    ): List<Pair<SourcedCategory, CategoryRandomSupplements>> {
        val presentCategories = categoriesWithMappers
            .filter { it.first.speechParts.contains(speechPart.type) }
            .filter { it.first.actualValues.isNotEmpty() }

        return presentCategories.flatMap { (c, s) ->
            c.affected.filter { it.speechPart == speechPart.type }.map {
                var compulsoryData = s.randomIsCompulsory(speechPart.type)
                if (c.actualValues.size <= 1)
                    compulsoryData = compulsoryData.copy(isCompulsory = false)

                val existingCoCategories = compulsoryData.compulsoryCoCategories.filter {
                    presentCategories
                        .firstOrNull { sc -> sc.first.outType == it.first().parentClassName }
                        ?.let { true }
                        ?: false
                }
                compulsoryData = compulsoryData.copy(compulsoryCoCategories = existingCoCategories)

                SourcedCategory(c, it.source, compulsoryData) to s
            }
        }
    }

    private fun generateIntransitiveVerbs(verbParadigm: SpeechPartChangeParadigm) = verbParadigm.copyForNewSpeechPart(
        Verb.toIntransitive(),
        mapOf(Agent to Argument)
    ) { c -> c.categories.none { it.source is RelationGranted && it.source.relation == Patient } }

    private fun checkCompulsoryConsistency(speechPartChangesMap: Map<TypedSpeechPart, SpeechPartChangeParadigm>) {
        var shouldCheck = true
        while (shouldCheck) {
            shouldCheck = false

            for (paradigm in speechPartChangesMap.values)
                for (sourcedCategory in paradigm.categories) {
                    if (sourcedCategory.category.outType in compulsoryConsistencyExceptions)
                        continue

                    if (sourcedCategory.source is RelationGranted && sourcedCategory.compulsoryData.isCompulsory) {
                        val relevantCategories = sourcedCategory.source.possibleSpeechParts
                            .flatMap { sp -> speechPartChangesMap.entries.filter { it.key.type == sp } }
                            .map { e ->
                                e.value.categories
                                    .firstOrNull { it.category == sourcedCategory.category }
                            }
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

    private val compulsoryConsistencyExceptions = listOf(inclusivityOutName)
}
