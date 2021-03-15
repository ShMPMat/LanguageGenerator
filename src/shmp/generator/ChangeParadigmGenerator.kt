package shmp.generator

import shmp.language.SpeechPart
import shmp.language.category.Category
import shmp.language.category.CategoryRandomSupplements
import shmp.language.category.definitenessName
import shmp.language.category.paradigm.ParametrizedCategory
import shmp.language.syntax.ChangeParadigm
import shmp.language.category.paradigm.SpeechPartChangeParadigm
import shmp.language.category.paradigm.WordChangeParadigm
import shmp.language.category.realization.WordCategoryApplicator
import shmp.language.phonology.RestrictionsParadigm
import shmp.language.phonology.prosody.ProsodyChangeParadigm
import shmp.language.phonology.prosody.StressType
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
        val syntaxParadigm = syntaxParadigmGenerator.generateSyntaxParadigm()
        val wordOrder = wordOrderGenerator.generateWordOrder(syntaxParadigm)

        return ChangeParadigm(
            wordOrder,
            wordChangeParadigm,
            syntaxParadigm
        )
    }

    private fun articlePresent(
        categories: List<Category>,
        speechPartChangesMap: MutableMap<SpeechPart, SpeechPartChangeParadigm>
    ): Boolean {
        if (categories.first { it.outType == definitenessName }.actualValues.isEmpty())
            return false

        return speechPartChangesMap.any { (_, u) ->
            u.applicators.values
                .flatMap { it.values }
                .any { it is WordCategoryApplicator && it.applicatorWord.semanticsCore.speechPart == SpeechPart.Article }
        }
    }
}
