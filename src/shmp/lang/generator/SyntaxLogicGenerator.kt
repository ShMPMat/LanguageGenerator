package shmp.lang.generator

import shmp.lang.language.CategoryValue
import shmp.lang.language.CategoryValues
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.category.*
import shmp.lang.language.category.NounClassValue.*
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
        //TODO handle different nominals
        val verbParadigms = changeParadigm.getSpeechPartParadigms(SpeechPart.Verb)
        val cases = changeParadigm.categories.first { it.outType == caseOutName }.actualValues


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
        .filterIsInstance<NounClass>()
        .firstOrNull()
        ?.takeIf { it.actualValues.isNotEmpty() }
        ?.let { genderCategory ->
            val genderCategorySolver = genderCategory.actualValues.map {
                it as NounClassValue
                it to it
            }.toMap().toMutableMap()

            val absentGenders = genderCategory.allPossibleValues
                .filter { it !in genderCategory.actualValues }
                .map { it as NounClassValue }

            for (gender in absentGenders) genderCategorySolver[gender] = when (gender) {
                Female -> listOf(NounClassValue.Person, Common, Neutral).first { it in genderCategory.actualValues }
                Male -> listOf(NounClassValue.Person, Common, Neutral).first { it in genderCategory.actualValues }
                Neutral -> listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
                Common -> Neutral.takeIf { it in genderCategory.actualValues }
                    ?: listOf(Female, Male).filter { it in genderCategory.actualValues }.randomElement()
                NounClassValue.Person -> listOf(Common, Neutral).firstOrNull() { it in genderCategory.actualValues }
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

    private fun generateDeixisCategorySolver(): Map<Pair<DeixisValue?, TypedSpeechPart>, CategoryValues> {
        val deixisCategorySolver: MutableMap<Pair<DeixisValue?, TypedSpeechPart>, CategoryValues> = mutableMapOf()

        for (speechPart in changeParadigm.speechParts) {
            val deixisValues = changeParadigm.getSpeechPartParadigm(speechPart)
                .getCategoryValues<Deixis>()
            val definitenessValues = changeParadigm.getSpeechPartParadigm(speechPart)
                .getCategoryValues<Definiteness>()


            val indefiniteArticleWrapped = definitenessValues
                .firstOrNull { it == DefinitenessValue.Indefinite }
                ?.let { mutableSetOf(it) }
            val definiteArticleWrapped = definitenessValues
                .firstOrNull { it == DefinitenessValue.Definite }
                ?.let { mutableSetOf(it) }

            val naiveSolver = deixisValues.map {
                it as DeixisValue
                it to mutableSetOf<CategoryValue>(it)
            }.toMap().toMutableMap<DeixisValue?, MutableSet<CategoryValue>>()

            naiveSolver[null] = mutableSetOf()

            if (deixisValues.isNotEmpty()) {
                val absentDeixis = changeParadigm.getSpeechPartParadigm(speechPart)
                    .getCategory<Deixis>()
                    .category
                    .allPossibleValues
                    .filter { it !in deixisValues }
                    .map { it as DeixisValue }

                for (deixis in absentDeixis) naiveSolver[deixis] = when (deixis) {
                    DeixisValue.Undefined -> indefiniteArticleWrapped
                        ?: mutableSetOf()
                    DeixisValue.Proximal -> definiteArticleWrapped
                        ?: mutableSetOf(DeixisValue.Undefined)
                    DeixisValue.Medial -> listOf(
                        mutableSetOf<CategoryValue>(DeixisValue.Proximal),
                        mutableSetOf<CategoryValue>(DeixisValue.Distant)
                    )
                        .filter { it.first() in deixisValues }.randomElementOrNull()
                        ?: definiteArticleWrapped
                        ?: mutableSetOf(DeixisValue.Undefined)
                    DeixisValue.Distant -> definiteArticleWrapped ?: mutableSetOf(DeixisValue.Undefined)
                    DeixisValue.ProximalAddressee -> listOf(
                        mutableSetOf<CategoryValue>(DeixisValue.Proximal),
                        mutableSetOf<CategoryValue>(DeixisValue.Distant)
                    )
                        .filter { it.first() in deixisValues }.randomElementOrNull()
                        ?: definiteArticleWrapped
                        ?: mutableSetOf(DeixisValue.Undefined)
                }

            }

            val definitenessNecessity = changeParadigm.getSpeechPartParadigm(speechPart)
                .getCategoryOrNull<Definiteness>()
                ?.isCompulsory ?: false

            if (definitenessNecessity)
                for (deixis in DeixisValue.values().toList() + listOf<DeixisValue?>(null)) when (deixis) {
                    null, DeixisValue.Undefined -> indefiniteArticleWrapped?.let {
                        naiveSolver[deixis] = (naiveSolver.getOrDefault(deixis, mutableSetOf()) + it).toMutableSet()
                    }
                    else -> definiteArticleWrapped?.let {
                        naiveSolver[deixis] = (naiveSolver.getOrDefault(deixis, mutableSetOf()) + it).toMutableSet()
                    }
                }

            for ((t, u) in naiveSolver) {
                deixisCategorySolver[t to speechPart] = u.toList()
            }
        }

        return deixisCategorySolver
    }

    private fun generatePersonalPronounDropSolver(): PersonalPronounDropSolver {
        val verbalCategories =
            changeParadigm.getSpeechPartParadigms(SpeechPart.Verb).first().categories//TODO bullshit decision
        val pronounCategories =
            changeParadigm.getSpeechPartParadigm(SpeechPart.PersonalPronoun.toUnspecified()).categories

        val personalPronounDropSolver = mutableListOf<Pair<ActorType, CategoryValues>>()

        for (actor in ActorType.values()) {
            val source = when (actor) {
                Agent -> SyntaxRelation.Agent
                Patient -> SyntaxRelation.Patient
            }

            val relevantCategories = verbalCategories
                .filter { it.source is CategorySource.RelationGranted && it.source.relation == source }

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
