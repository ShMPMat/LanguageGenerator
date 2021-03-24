package shmp.lang.generator

import shmp.lang.language.CategoryValues
import shmp.lang.language.SpeechPart
import shmp.lang.language.category.*
import shmp.lang.language.category.NumbersValue.*
import shmp.lang.language.category.paradigm.ParametrizedCategory
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.category.paradigm.SpeechPartChangeParadigm
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.category.realization.WordCategoryApplicator
import shmp.lang.language.phonology.RestrictionsParadigm
import shmp.lang.language.phonology.prosody.ProsodyChangeParadigm
import shmp.lang.language.phonology.prosody.StressType
import shmp.lang.language.syntax.SyntaxLogic
import shmp.lang.language.syntax.context.ContextValue
import shmp.random.singleton.chanceOf
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
        val syntaxLogic = generateSyntaxLogic(wordChangeParadigm)

        return ChangeParadigm(
            wordOrder,
            wordChangeParadigm,
            syntaxParadigm,
            syntaxLogic
        )
    }

    private fun generateSyntaxLogic(changeParadigm: WordChangeParadigm): SyntaxLogic {
        val verbFormSolver = mutableMapOf<ContextValue.TimeContext, CategoryValues>()

        changeParadigm.getSpeechPartParadigm(SpeechPart.Verb).categories
            .map { it.category }
            .filterIsInstance<Tense>()
            .firstOrNull()
            ?.actualValues
            ?.firstOrNull { it as TenseValue == TenseValue.Present }
            ?.let {
                verbFormSolver[ContextValue.TimeContext.Regular] = listOf(it)
            }

        val numberCategorySolver = changeParadigm.categories
            .filterIsInstance<Numbers>()
            .firstOrNull()
            ?.let { numbersCategory ->
                val numberCategorySolver = numbersCategory.actualValues.map {
                    it as NumbersValue
                    it to when(it) {
                        Singular -> 1..1
                        Dual -> 2..2
                        Plural -> 2..Int.MAX_VALUE
                    }
                }.toMap().toMutableMap()

                if (Dual in numbersCategory.actualValues)
                    numberCategorySolver[Plural] = 3..Int.MAX_VALUE
                else 0.05.chanceOf {
                    numberCategorySolver[Plural] = 3..Int.MAX_VALUE
                    numberCategorySolver[Singular] = 1..2
                }

                numberCategorySolver
            }

        return SyntaxLogic(verbFormSolver, numberCategorySolver)
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
