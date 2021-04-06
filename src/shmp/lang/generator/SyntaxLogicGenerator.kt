package shmp.lang.generator

import shmp.lang.language.CategoryValue
import shmp.lang.language.CategoryValues
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.category.*
import shmp.lang.language.category.GenderValue.*
import shmp.lang.language.category.NumbersValue.*
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.syntax.*
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.context.ActorType.*
import shmp.lang.language.syntax.context.ContextValue
import shmp.lang.language.syntax.features.CopulaType
import shmp.lang.utils.listCartesianProduct
import shmp.random.singleton.*
import shmp.random.toSampleSpaceObject


class SyntaxLogicGenerator(val changeParadigm: WordChangeParadigm, val syntaxParadigm: SyntaxParadigm) {
    fun generateSyntaxLogic() = SyntaxLogic(
        generateVerbFormSolver(),
        generateVerbCaseSolver(),
        generateCopulaCasesSolver(),
        generateNumberCategorySolver(),
        generateGenderCategorySolver(),
        generateDeixisCategorySolver(),
        generatePersonalPronounDropSolver()
    )

    private fun generateCopulaCasesSolver(): Map<Pair<Pair<CopulaType, SyntaxRelation>, TypedSpeechPart>, CategoryValues> {
        val copulaCasesSolver: MutableMap<Pair<Pair<CopulaType, SyntaxRelation>, TypedSpeechPart>, CategoryValues> =
            mutableMapOf()

        val copulas = syntaxParadigm.copulaPresence.copulaType.map { it.feature }
        val nominals = changeParadigm.getSpeechPartParadigms(SpeechPart.Noun) +
                changeParadigm.getSpeechPartParadigms(SpeechPart.PersonalPronoun) +
                changeParadigm.getSpeechPartParadigms(SpeechPart.DeixisPronoun)

        for (speechPartParadigm in nominals) {
            for (copula in copulas) {
                copulaCasesSolver[copula to SyntaxRelation.Agent to speechPartParadigm.speechPart] = listOf(
                    CaseValue.Nominative.toSampleSpaceObject(0.9),
                    CaseValue.Absolutive.toSampleSpaceObject(0.9),
                    CaseValue.Ergative.toSampleSpaceObject(0.1),
                    CaseValue.Accusative.toSampleSpaceObject(0.1)
                )
                    .filter { it.value in speechPartParadigm.getCategoryValues<Case>() }
                    .randomUnwrappedElementOrNull()
                    ?.let { listOf(it) }
                    ?: emptyList()

                copulaCasesSolver[copula to SyntaxRelation.SubjectCompliment to speechPartParadigm.speechPart] = listOf(
                    CaseValue.Nominative.toSampleSpaceObject(0.5),
                    CaseValue.Absolutive.toSampleSpaceObject(0.5),
                    CaseValue.Ergative.toSampleSpaceObject(0.5),
                    CaseValue.Accusative.toSampleSpaceObject(0.5)
                )
                    .filter { it.value in speechPartParadigm.getCategoryValues<Case>() }
                    .randomUnwrappedElementOrNull()
                    ?.let { listOf(it) }
                    ?: emptyList()
            }
        }

        return copulaCasesSolver
    }

    private fun generateVerbFormSolver(): MutableMap<VerbContextInfo, List<CategoryValue>> {
        val verbFormSolver: MutableMap<VerbContextInfo, List<CategoryValue>> = mutableMapOf()

        val verbalSpeechParts = changeParadigm.getSpeechParts(SpeechPart.Verb)

        for (speechPart in verbalSpeechParts)
            changeParadigm.getSpeechPartParadigm(speechPart).categories
                .map { it.category }
                .filterIsInstance<Tense>()
                .firstOrNull()
                ?.actualValues
                ?.firstOrNull { it as TenseValue == TenseValue.Present }
                ?.let {
                    verbFormSolver[speechPart to ContextValue.TimeContext.Regular] = listOf(it)
                }

        return verbFormSolver
    }

    private fun generateVerbCaseSolver(): Map<Pair<Pair<TypedSpeechPart, Set<CategoryValue>>, SyntaxRelation/*TODO depend on speech part too*/>, CategoryValues> {
        val result: MutableMap<Pair<Pair<TypedSpeechPart, Set<CategoryValue>>, SyntaxRelation>, CategoryValues> =
            mutableMapOf()
        //TODO handle split
        val verbParadigms = changeParadigm.getSpeechPartParadigms(SpeechPart.Verb)
        val cases = changeParadigm.categories.filterIsInstance<Case>().first().actualValues


        for (verbTypeParadigm in verbParadigms) {
            val verbType = verbTypeParadigm.speechPart
            val times = verbTypeParadigm.categories
                .firstOrNull { it.category is Tense }
                ?.category?.actualValues
                ?.map { setOf(it) }
                ?: listOf(setOf())

            for (time in times)
                if (CaseValue.Nominative in cases && CaseValue.Accusative in cases) {
                    result[verbType to time to SyntaxRelation.Agent] = listOf(CaseValue.Nominative)
                    result[verbType to time to SyntaxRelation.Argument] = listOf(CaseValue.Nominative)
                    result[verbType to time to SyntaxRelation.Patient] = listOf(CaseValue.Accusative)
                } else if (CaseValue.Ergative in cases && CaseValue.Absolutive in cases) {
                    result[verbType to time to SyntaxRelation.Agent] = listOf(CaseValue.Ergative)
                    result[verbType to time to SyntaxRelation.Argument] = listOf(CaseValue.Absolutive)
                    result[verbType to time to SyntaxRelation.Patient] = listOf(CaseValue.Absolutive)
                } else {
                    result[verbType to time to SyntaxRelation.Agent] = listOf()
                    result[verbType to time to SyntaxRelation.Argument] = listOf()
                    result[verbType to time to SyntaxRelation.Patient] = listOf()
                }
        }

        return result
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

            for (deixis in absentDeixis) deixisCategorySolver[deixis] = when (deixis) {
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
        val verbalCategories =
            changeParadigm.getSpeechPartParadigms(SpeechPart.Verb).first().categories//TODO bullshit decision
        val pronounCategories =
            changeParadigm.getSpeechPartParadigm(SpeechPart.PersonalPronoun.toUnspecified()).categories

        val personalPronounDropSolver = mutableListOf<Pair<ActorType, CategoryValues>>()

        for (actor in ActorType.values()) {
            val source = when (actor) {
                Agent -> CategorySource.RelationGranted(SyntaxRelation.Agent)
                Patient -> CategorySource.RelationGranted(SyntaxRelation.Patient)
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
