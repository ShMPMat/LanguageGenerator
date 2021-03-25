package shmp.lang.generator

import shmp.lang.language.CategoryValue
import shmp.lang.language.SpeechPart
import shmp.lang.language.category.*
import shmp.lang.language.category.GenderValue.*
import shmp.lang.language.category.NumbersValue.*
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.syntax.SyntaxLogic
import shmp.lang.language.syntax.context.ContextValue
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement


class SyntaxLogicGenerator(val changeParadigm: WordChangeParadigm) {
    fun generateSyntaxLogic(): SyntaxLogic {
        val verbFormSolver = generateVerbFormSolver()

        val numberCategorySolver = generateNumberCategorySolver()

        val genderCategorySolver = generateGenderCategorySolver()

        return SyntaxLogic(verbFormSolver, numberCategorySolver, genderCategorySolver)
    }

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
                Neutral -> listOf(Female, Male).randomElement()
                Common -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).randomElement()
                GenderValue.Person -> listOf(Common, Neutral).first { it in genderCategory.actualValues }
                Plant -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).randomElement()
                Fruit -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).randomElement()
                LongObject -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).randomElement()
            }

            genderCategorySolver
        }
}
