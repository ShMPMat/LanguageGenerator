package shmp.lang.generator

import shmp.lang.language.category.Category
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.definitenessName
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.SpeechPartChangeParadigm
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.category.realization.WordCategoryApplicator
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.prosody.ProsodyChangeParadigm
import shmp.lang.language.phonology.prosody.StressType
import shmp.lang.language.syntax.ChangeParadigm
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

    internal fun generateChangeParadigm(
        categoriesWithMappers: List<Pair<Category, CategoryRandomSupplements>>
    ): ChangeParadigm {
        val categories = categoriesWithMappers.map { it.first }

        val oldSpeechParts = mutableListOf<TypedSpeechPart>()
        val speechParts = SpeechPart.values().map { it.toUnspecified() }.toMutableList()
        val newSpeechParts = mutableSetOf<TypedSpeechPart>()

        val speechPartChangesMap = mutableMapOf<TypedSpeechPart, SpeechPartChangeParadigm>()

        while (speechParts.isNotEmpty()) {
            oldSpeechParts.addAll(speechParts)
            speechParts.map { speechPart ->
                val restrictions = restrictionsParadigm.restrictionsMapper.getValue(speechPart)
                val speechPartCategoriesAndSupply = categoriesWithMappers
                    .filter { it.first.speechParts.contains(speechPart.type) }
                    .filter { it.first.actualValues.isNotEmpty() }
                    .flatMap { (c, s) ->
                        c.affected
                            .filter { it.speechPart == speechPart.type }
                            .map { SourcedCategory(c, it.source) to s }
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

                val orderedApplicators = speechPartApplicatorsGenerator.randomApplicatorsOrder(applicators)
                val changeParadigm = SpeechPartChangeParadigm(
                    speechPart,
                    orderedApplicators,
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

        if (!articlePresent(categories, speechPartChangesMap)) {
            speechPartChangesMap[SpeechPart.Article.toUnspecified()] =
                SpeechPartChangeParadigm(
                    SpeechPart.Article.toUnspecified(),
                    listOf(),
                    mapOf(),
                    speechPartChangesMap.getValue(SpeechPart.Article.toUnspecified()).prosodyChangeParadigm
                )
        }

        val wordChangeParadigm = WordChangeParadigm(categories, speechPartChangesMap)
        val syntaxParadigm = syntaxParadigmGenerator.generateSyntaxParadigm()
        val wordOrder = wordOrderGenerator.generateWordOrder(syntaxParadigm)
        val syntaxLogic = SyntaxLogicGenerator(wordChangeParadigm).generateSyntaxLogic()

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
                .any { it is WordCategoryApplicator && it.applicatorWord.semanticsCore.speechPart.type == SpeechPart.Article }
        }
    }
}
