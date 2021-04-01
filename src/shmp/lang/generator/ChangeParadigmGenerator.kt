package shmp.lang.generator

import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.category.Category
import shmp.lang.language.category.CategoryRandomSupplements
import shmp.lang.language.category.definitenessName
import shmp.lang.language.category.paradigm.ParametrizedCategory
import shmp.lang.language.category.paradigm.SpeechPartChangeParadigm
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.category.realization.WordCategoryApplicator
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.prosody.ProsodyChangeParadigm
import shmp.lang.language.phonology.prosody.StressType
import shmp.lang.language.syntax.ChangeParadigm
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
        val speechPartChangesMap = SpeechPart.values()
            .map { it.toUnspecified() }
            .map { speechPart ->
                val speechPartCategoriesAndSupply = categoriesWithMappers
                    .filter { it.first.speechParts.contains(speechPart.type) }
                    .filter { it.first.actualValues.isNotEmpty() }
                    .flatMap { (c, s) ->
                        c.affected
                            .filter { it.speechPart == speechPart.type }
                            .map { ParametrizedCategory(c, it.source) to s }
                    }
                val applicators = speechPartApplicatorsGenerator
                    .randomApplicatorsForSpeechPart(
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
