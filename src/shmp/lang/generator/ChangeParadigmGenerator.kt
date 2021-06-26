package shmp.lang.generator

import shmp.lang.language.category.Category
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.definitenessName
import shmp.lang.language.category.paradigm.*
import shmp.lang.language.category.realization.WordCategoryApplicator
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.toIntransitive
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.prosody.ProsodyChangeParadigm
import shmp.lang.language.phonology.prosody.StressType
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.syntax.SyntaxRelation
import kotlin.math.max
import kotlin.random.Random


class ChangeParadigmGenerator(
    private val stressPattern: StressType,
    lexisGenerator: LexisGenerator,
    changeGenerator: ChangeGenerator,
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random
) {
    val speechPartApplicatorsGenerator = SpeechPartApplicatorsGenerator(lexisGenerator, changeGenerator, random)
    val wordOrderGenerator = WordOrderGenerator(random)
    val syntaxParadigmGenerator = SyntaxParadigmGenerator()

    internal fun generateChangeParadigm(categoriesWithMappers: List<SupplementedCategory>): ChangeParadigm {
        val categories = categoriesWithMappers.map { it.first }

        val oldSpeechParts = mutableListOf<TypedSpeechPart>()
        val speechParts = values().map { it.toUnspecified() }.toMutableList()
        val newSpeechParts = mutableSetOf<TypedSpeechPart>()

        val speechPartChangesMap = mutableMapOf<TypedSpeechPart, SpeechPartChangeParadigm>()

        while (speechParts.isNotEmpty()) {
            oldSpeechParts.addAll(speechParts)
            speechParts.map { speechPart ->
                val restrictions = restrictionsParadigm.restrictionsMapper.getValue(speechPart)
                val presentCategories = categoriesWithMappers
                    .filter { it.first.speechParts.contains(speechPart.type) }
                    .filter { it.first.actualValues.isNotEmpty() }
                val speechPartCategoriesAndSupply = presentCategories
                    .flatMap { (c, s) ->
                        c.affected
                            .filter { it.speechPart == speechPart.type }
                            .map {
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
                val (words, applicators) = speechPartApplicatorsGenerator
                    .randomApplicatorsForSpeechPart(
                        speechPart,
                        restrictions,
                        speechPartCategoriesAndSupply
                    )
                words.forEach {
                    if (it.semanticsCore.speechPart !in oldSpeechParts)
                        newSpeechParts.add(it.semanticsCore.speechPart)
                }

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
        val newApplicators = verbParadigm.applicators.mapNotNull { (cluster, applicator) ->
            if (cluster.categories.any { it.source is RelationGranted && it.source.relation == SyntaxRelation.Patient })
                return@mapNotNull null

            val newCategories = cluster.categories.map {
                it.copy(
                    source = if (it.source is RelationGranted && it.source.relation == SyntaxRelation.Agent)
                        it.source.copy(relation = SyntaxRelation.Argument)
                    else it.source
                )
            }
            val newClusterValues = cluster.possibleValues.map { v ->
                v.categoryValues.map { cv ->
                    newCategories.flatMap { it.actualSourcedValues }.first { cv.categoryValue == it.categoryValue }
                }
            }.toSet()
            val newCluster = ExponenceCluster(newCategories, newClusterValues)

            newCluster to applicator.map { (value, applicator) ->
                val newValue = newCluster.possibleValues
                    .first { nv -> value.categoryValues.all { c -> c.categoryValue in nv.categoryValues.map { it.categoryValue } } }

                newValue to applicator
            }.toMap()
        }.toMap()
        speechPartChangesMap[Verb.toIntransitive()] = SpeechPartChangeParadigm(
            Verb.toIntransitive(),
            speechPartApplicatorsGenerator.randomApplicatorsOrder(newApplicators),
            newApplicators,
            verbParadigm.prosodyChangeParadigm
        )
        val verbRestrictions = restrictionsParadigm.restrictionsMapper.getValue(Verb.toUnspecified())
        restrictionsParadigm.restrictionsMapper[Verb.toIntransitive()] = verbRestrictions.copy()


        for (paradigm in speechPartChangesMap.values)
            for (sourcedCategory in paradigm.categories)
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

                    sourcedCategory.compulsoryData = CompulsoryData(areAllRelationsCompulsory, allCoCategories)
                }

        if (!articlePresent(categories, speechPartChangesMap)) {
            speechPartChangesMap[Article.toUnspecified()] =
                SpeechPartChangeParadigm(
                    Article.toUnspecified(),
                    listOf(),
                    mapOf(),
                    speechPartChangesMap.getValue(Article.toUnspecified()).prosodyChangeParadigm
                )
        }

        val wordChangeParadigm = WordChangeParadigm(categories, speechPartChangesMap)
        val syntaxParadigm = syntaxParadigmGenerator.generateSyntaxParadigm(wordChangeParadigm)
        val wordOrder = wordOrderGenerator.generateWordOrder(syntaxParadigm)
        val syntaxLogic = SyntaxLogicGenerator(wordChangeParadigm, syntaxParadigm).generateSyntaxLogic()

        return ChangeParadigm(
            wordOrder,
            wordChangeParadigm,
            syntaxParadigm,
            syntaxLogic
        )
    }

    private fun articlePresent(
        categories: List<Category>,
        speechPartChangesMap: MutableMap<TypedSpeechPart, SpeechPartChangeParadigm>
    ): Boolean {
        if (categories.first { it.outType == definitenessName }.actualValues.isEmpty())
            return false

        return speechPartChangesMap.any { (_, u) ->
            u.applicators.values
                .flatMap { it.values }
                .any { it is WordCategoryApplicator && it.applicatorWord.semanticsCore.speechPart.type == Article }
        }
    }
}
