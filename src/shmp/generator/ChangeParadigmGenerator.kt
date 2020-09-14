package shmp.generator

import shmp.language.SpeechPart
import shmp.language.category.Category
import shmp.language.category.CategoryRandomSupplements
import shmp.language.category.definitenessName
import shmp.language.category.paradigm.ParametrizedCategory
import shmp.language.syntax.SyntaxParadigm
import shmp.language.category.paradigm.SpeechPartChangeParadigm
import shmp.language.category.paradigm.WordChangeParadigm
import shmp.language.category.realization.WordCategoryApplicator
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.prosody.ProsodyChangeParadigm
import shmp.language.phonology.prosody.StressType
import shmp.language.syntax.*
import shmp.language.syntax.clause.translation.SentenceType
import shmp.language.syntax.clause.translation.differentWordOrderProbability
import shmp.random.randomElement
import shmp.random.randomSublist
import shmp.random.testProbability
import kotlin.random.Random


class ChangeParadigmGenerator(
    private val stressPattern: StressType,
    lexisGenerator: LexisGenerator,
    changeGenerator: ChangeGenerator,
    private val restrictionsParadigm: RestrictionsParadigm,
    private val random: Random
) {
    val speechPartApplicatorsGenerator = SpeechPartApplicatorsGenerator(lexisGenerator, changeGenerator, random)

    fun generateChangeParadigm(
        categoriesWithMappers: List<Pair<Category, CategoryRandomSupplements>>
    ): SyntaxParadigm {
        val categories = categoriesWithMappers.map { it.first }
        val speechPartChangesMap = SpeechPart.values().map { speechPart ->
            val speechPartCategoriesAndSupply = categoriesWithMappers
                .filter { it.first.speechParts.contains(speechPart) }
                .filter { it.first.actualValues.isNotEmpty() }
                .flatMap { (c, s) ->
                    c.affected
                        .filter { it.speechPart == speechPart }
                        .map { ParametrizedCategory(c, it.source) to s }
                }
            val applicators = speechPartApplicatorsGenerator.randomApplicatorsForSpeechPart(
                speechPart,
                restrictionsParadigm.restrictionsMapper.getValue(speechPart),
                speechPartCategoriesAndSupply
            )
            val orderedApplicators = speechPartApplicatorsGenerator.randomApplicatorsOrder(applicators)
            speechPart to SpeechPartChangeParadigm(
                speechPart,
                orderedApplicators,
                applicators,
                ProsodyChangeParadigm(stressPattern)
            )
        }.toMap().toMutableMap()

        if (!articlePresent(categories, speechPartChangesMap)) {
            speechPartChangesMap[SpeechPart.Article] =
                SpeechPartChangeParadigm(
                    SpeechPart.Article,
                    listOf(),
                    mapOf(),
                    speechPartChangesMap.getValue(SpeechPart.Article).prosodyChangeParadigm
                )
        }

        val wordChangeParadigm = WordChangeParadigm(categories, speechPartChangesMap)
        val wordOrder = generateWordOrder()
        return SyntaxParadigm(wordOrder, wordChangeParadigm)
    }

    private fun generateWordOrder(): WordOrder {
        val sovOrder = generateSovOrder()
        val nominalGroupOrder = randomElement(NominalGroupOrder.values(), random)

        return WordOrder(sovOrder, nominalGroupOrder)
    }

    private fun generateSovOrder(): Map<SentenceType, SovOrder> {
        val mainOrder = generateSimpleSovOrder()
        val resultMap = mutableMapOf(SentenceType.MainClause to mainOrder)

        for (sentenceType in SentenceType.values().filter { it != SentenceType.MainClause }) {
            resultMap[sentenceType] =
                if (testProbability(differentWordOrderProbability(sentenceType), random))
                    generateSimpleSovOrder()
                else mainOrder
        }

        return resultMap
    }

    private fun generateSimpleSovOrder(): SovOrder {
        val basicTemplate = randomElement(BasicSovOrder.values(), random)

        val (references, name) = when (basicTemplate) {
            BasicSovOrder.Two -> {
                val (t1, t2) = randomSublist(
                    BasicSovOrder.values().take(6),
                    { it.probability },
                    random,
                    2,
                    3
                )
                val referenceOrder = ({ r: Random ->
                    (if (r.nextBoolean()) t1 else t2).referenceOrder(r)
                })
                referenceOrder to "$t1 or $t2"
            }
            else -> basicTemplate.referenceOrder to basicTemplate.name
        }

        return SovOrder(references, name)
    }

    private fun articlePresent(
        categories: List<Category>,
        speechPartChangesMap: MutableMap<SpeechPart, SpeechPartChangeParadigm>
    ): Boolean {
        if (categories.first { it.outType == definitenessName }.actualValues.isEmpty()) return false
        return speechPartChangesMap.any { (_, u) ->
            u.applicators.values
                .flatMap { it.values }
                .any { it is WordCategoryApplicator && it.applicatorWord.semanticsCore.speechPart == SpeechPart.Article }
        }
    }
}
