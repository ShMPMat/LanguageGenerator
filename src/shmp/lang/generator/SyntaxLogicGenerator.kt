package shmp.lang.generator

import shmp.lang.language.CategoryValue
import shmp.lang.language.CategoryValues
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.category.*
import shmp.lang.language.category.GenderValue.*
import shmp.lang.language.category.NumbersValue.*
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.syntax.PersonalPronounDropSolver
import shmp.lang.language.syntax.SyntaxLogic
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.context.ActorType.*
import shmp.lang.language.syntax.context.ContextValue
import shmp.lang.utils.listCartesianProduct
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull


class SyntaxLogicGenerator(val changeParadigm: WordChangeParadigm) {
    fun generateSyntaxLogic() = SyntaxLogic(
        generateVerbFormSolver(),
        generateNumberCategorySolver(),
        generateGenderCategorySolver(),
        generateDeixisCategorySolver(),
        generatePersonalPronounDropSolver()
    )

    private fun generateVerbFormSolver(): MutableMap<ContextValue.TimeContext, List<CategoryValue>> {
        val verbFormSolver: MutableMap<ContextValue.TimeContext, List<CategoryValue>> = mutableMapOf()

        changeParadigm.getSpeechPartParadigm(SpeechPart.Verb).categories
            .map { it.category }
            .filterIsInstance<Tense>()
            .firstOrNull()
            ?.actualValues
            ?.firstOrNull { it as TenseValue == TenseValue.Present }
            ?.let {
                verbFormSolver[ContextValue.TimeContext.Regular] = listOf(it)
            }

        return verbFormSolver
    }

    private fun generateNumberCategorySolver() = changeParadigm.categories
        .filterIsInstance<Numbers>()
        .firstOrNull()
        ?.takeIf { it.actualValues.isNotEmpty() }
        ?.let { numbersCategory ->
            val numberCategorySolver = numbersCategory.actualValues.map {
                it as NumbersValue
                it to when (it) {
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

    private fun generateGenderCategorySolver() = changeParadigm.categories
        .filterIsInstance<Gender>()
        .firstOrNull()
        ?.takeIf { it.actualValues.isNotEmpty() }
        ?.let { genderCategory ->
            val genderCategorySolver = genderCategory.actualValues.map {
                it as GenderValue
                it to it
            }.toMap().toMutableMap()

            val absentGenders = genderCategory.allPossibleValues
                .filter { it !in genderCategory.actualValues }
                .map { it as GenderValue }

            for (gender in absentGenders) genderCategorySolver[gender] = when (gender) {
                Female -> listOf(GenderValue.Person, Common, Neutral).first { it in genderCategory.actualValues }
                Male -> listOf(GenderValue.Person, Common, Neutral).first { it in genderCategory.actualValues }
                Neutral -> listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
                Common -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
                GenderValue.Person -> listOf(Common, Neutral).firstOrNull() { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
                Plant -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
                Fruit -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
                LongObject -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
            }

            genderCategorySolver
        }

    private fun generateDeixisCategorySolver(): Map<DeixisValue?, List<CategoryValue>> = changeParadigm.categories
        .filterIsInstance<Deixis>()
        .first()
        .let { deixisCategory ->
            val definitenessCategory = changeParadigm.categories
                .filterIsInstance<Definiteness>()
                .firstOrNull()
            val indefiniteArticleWrapped = definitenessCategory?.actualValues
                ?.firstOrNull { it == DefinitenessValue.Indefinite }
                ?.let { listOf(it) }
            val definiteArticleWrapped = definitenessCategory?.actualValues
                ?.firstOrNull { it == DefinitenessValue.Definite }
                ?.let { listOf(it) }

            val deixisCategorySolver = deixisCategory.actualValues.map {
                it as DeixisValue
                it to listOf(it)
            }.toMap().toMutableMap<DeixisValue?, List<CategoryValue>>()

            deixisCategorySolver[null] = listOf()

            val absentDeixis = deixisCategory.allPossibleValues
                .filter { it !in deixisCategory.actualValues }
                .map { it as DeixisValue }

            for (deixis in absentDeixis) deixisCategorySolver[deixis] = when(deixis) {
                DeixisValue.Undefined -> indefiniteArticleWrapped
                    ?: listOf()
                DeixisValue.Proximal -> definiteArticleWrapped
                    ?: listOf(DeixisValue.Undefined)
                DeixisValue.Medial -> listOf(listOf(DeixisValue.Proximal), listOf(DeixisValue.Distant))
                    .filter { it[0] in deixisCategory.actualValues }.randomElementOrNull()
                    ?: definiteArticleWrapped
                    ?: listOf(DeixisValue.Undefined)
                DeixisValue.Distant -> definiteArticleWrapped ?: listOf(DeixisValue.Undefined)
                DeixisValue.ProximalAddressee -> listOf(listOf(DeixisValue.Proximal), listOf(DeixisValue.Distant))
                    .filter { it[0] in deixisCategory.actualValues }.randomElementOrNull()
                    ?: definiteArticleWrapped
                    ?: listOf(DeixisValue.Undefined)
            }

            deixisCategorySolver
        }

    private fun generatePersonalPronounDropSolver(): PersonalPronounDropSolver {
        val verbalCategories = changeParadigm.getSpeechPartParadigm(SpeechPart.Verb).categories
        val pronounCategories = changeParadigm.getSpeechPartParadigm(SpeechPart.PersonalPronoun).categories

        val personalPronounDropSolver = mutableListOf<Pair<ActorType, CategoryValues>>()

        for (actor in ActorType.values()) {
            val source = when (actor) {
                Agent -> CategorySource.RelationGranted(SyntaxRelation.Subject)
                Patient -> CategorySource.RelationGranted(SyntaxRelation.Object)
            }

            val relevantCategories = verbalCategories.filter { it.source == source }

            if (relevantCategories.size == pronounCategories.size) 0.5.chanceOf {
                listCartesianProduct(pronounCategories.map { it.category.actualValues })
                    .forEach { personalPronounDropSolver.add(actor to it) }

            } else if (relevantCategories.isNotEmpty()) 0.5.chanceOf {
                listCartesianProduct(pronounCategories.map { it.category.actualValues })
                    .randomElement()
                    .let { personalPronounDropSolver.add(actor to it) }
            }
        }

        return personalPronounDropSolver
    }
}
