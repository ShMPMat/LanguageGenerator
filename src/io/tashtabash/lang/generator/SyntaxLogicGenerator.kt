package io.tashtabash.lang.generator

import io.tashtabash.lang.generator.supplement.additionalVerbTypes
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.DeixisValue.*
import io.tashtabash.lang.language.category.NounClassValue.*
import io.tashtabash.lang.language.category.Number
import io.tashtabash.lang.language.category.NumberValue.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.lexis.defaultSubtype
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.clause.description.AdjunctType
import io.tashtabash.lang.language.syntax.clause.description.MainObjectType
import io.tashtabash.lang.language.syntax.clause.description.ObjectType
import io.tashtabash.lang.language.syntax.context.ContextValue
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.utils.listCartesianProduct
import io.tashtabash.lang.utils.values
import io.tashtabash.random.singleton.*
import io.tashtabash.random.toSampleSpaceObject
import io.tashtabash.random.withProb


class SyntaxLogicGenerator(val changeParadigm: WordChangeParadigm, val syntaxParadigm: SyntaxParadigm) {
    private val nominalParadigms = changeParadigm.getSpeechPartParadigms(Noun) +
            changeParadigm.getSpeechPartParadigms(PersonalPronoun) +
            changeParadigm.getSpeechPartParadigms(DeixisPronoun)

    fun generateSyntaxLogic() = SyntaxLogic(
        generateVerbFormSolver(),
        generateVerbArgumentSolver(),
        generateVerbCaseSolver(),
        generateCopulaCaseSolver(),
        generateSyntaxRelationSolver(),
        generateNumberCategorySolver(),
        generateGenderCategorySolver(),
        generateDeixisCategorySolver(),
        generatePersonalPronounDropSolver(),
        changeParadigm.getSpeechPartParadigm(PersonalPronoun.toDefault()).getCategoryOrNull(inclusivityName)
    )

    private fun generateCopulaCaseSolver(): Map<Pair<Pair<CopulaType, SyntaxRelation>, TypedSpeechPart>, CategoryValues> {
        val copulaCaseSolver: MutableMap<Pair<Pair<CopulaType, SyntaxRelation>, TypedSpeechPart>, CategoryValues> =
            mutableMapOf()

        val copulas = syntaxParadigm.copulaPresence.copulaType.map { it.feature }

        for (speechPartParadigm in nominalParadigms)
            for (copula in copulas) {
                copulaCaseSolver[copula to SyntaxRelation.Agent to speechPartParadigm.speechPart] = listOf(
                    CaseValue.Nominative.toSampleSpaceObject(0.9),
                    CaseValue.Absolutive.toSampleSpaceObject(0.9),
                    CaseValue.Ergative.toSampleSpaceObject(0.1),
                    CaseValue.Accusative.toSampleSpaceObject(0.1)
                )
                    .filter { it.value in speechPartParadigm.getCategoryValues(caseName) }
                    .randomElementOrNull()
                    ?.value
//                    .randomUnwrappedElementOrNull() //TODO out T in the generic
                    ?.let { listOf(it) }
                    ?: emptyList()

                copulaCaseSolver[copula to SyntaxRelation.SubjectCompliment to speechPartParadigm.speechPart] = listOf(
                    CaseValue.Nominative.toSampleSpaceObject(0.5),
                    CaseValue.Absolutive.toSampleSpaceObject(0.5),
                    CaseValue.Ergative.toSampleSpaceObject(0.5),
                    CaseValue.Accusative.toSampleSpaceObject(0.5)
                )
                    .filter { it.value in speechPartParadigm.getCategoryValues(caseName) }
                    .randomElementOrNull()
                    ?.value
//                    .randomUnwrappedElementOrNull() //TODO out T in the generic
                    ?.let { listOf(it) }
                    ?: emptyList()
            }

        return copulaCaseSolver
    }

    private fun findCaseWrapped(caseValues: List<CategoryValue>, caseValue: CaseValue): List<CategoryValue>? =
        caseValues.firstOrNull { it == caseValue }
        ?.let { listOf(it) }

    private fun findAdpositionForCase(adpositionValues: List<CategoryValue>, caseValue: CaseValue): List<CategoryValue> {


        return adpositionValues.firstOrNull { it.semanticsCore.meaningCluster == caseValue.semanticsCore.meaningCluster }
            ?.let { listOf(it) }
            ?: emptyList()
    }

    private fun generateSyntaxRelationSolver(): Map<Pair<SyntaxRelation, TypedSpeechPart>, CategoryValues> {
        val syntaxRelationSolver: MutableMap<Pair<SyntaxRelation, TypedSpeechPart>, CategoryValues> = mutableMapOf()

        for (speechPartParadigm in nominalParadigms) {
            val caseValues = speechPartParadigm.getCategoryValues(caseName)
            val adpositionValues = speechPartParadigm.getCategoryValues(adpositionName)

            val governedCase = generateAdpositionGovernanceCase(speechPartParadigm.getCategoryOrNull(caseName))

            for ((case, syntaxRelation) in AdjunctType.entries.map { it.caseValue to it.relation }) {
                syntaxRelationSolver[syntaxRelation to speechPartParadigm.speechPart] = findCaseWrapped(caseValues, case)
                    ?: (governedCase + findAdpositionForCase(adpositionValues, case))

                // Equate Ben with Dat
                if (syntaxRelation == SyntaxRelation.Benefactor) 0.5.chanceOf {
                    syntaxRelationSolver[SyntaxRelation.Benefactor to speechPartParadigm.speechPart] = findCaseWrapped(caseValues, CaseValue.Dative)
                        ?: (governedCase + findAdpositionForCase(adpositionValues, CaseValue.Dative))
                }
            }

            syntaxRelationSolver[SyntaxRelation.Possessor to speechPartParadigm.speechPart] = findCaseWrapped(caseValues, CaseValue.Genitive)
                ?: (governedCase + findAdpositionForCase(adpositionValues, CaseValue.Genitive))
        }

        return syntaxRelationSolver
    }

    private fun generateAdpositionGovernanceCase(caseCategory: SourcedCategory?): List<CategoryValue> {
        // Adposition governs no case when it isn't compulsory
        if (caseCategory?.compulsoryData?.isCompulsory != true) 0.5.chanceOf {
            return listOf()
        }
        val caseValues = caseCategory?.category?.actualValues ?: emptyList()

        return findCaseWrapped(caseValues, CaseValue.Oblique)
            ?: findCaseWrapped(caseValues, CaseValue.Absolutive)
            ?: findCaseWrapped(caseValues, CaseValue.Nominative)
            ?: emptyList()
    }

    private fun generateVerbFormSolver(): Map<VerbContextInfo, SourcedCategoryValues> {
        val verbFormSolver: MutableMap<VerbContextInfo, SourcedCategoryValues> = mutableMapOf()

        val verbalSpeechParts = changeParadigm.getSpeechParts(Verb)

        for (speechPart in verbalSpeechParts)
            changeParadigm.getSpeechPartParadigm(speechPart).categories
                .firstOrNull { it.category.outType == tenseName }
                ?.actualSourcedValues
                ?.firstOrNull { it.categoryValue == TenseValue.Present }
                ?.let {
                    verbFormSolver[speechPart to ContextValue.TimeContext.Regular] = listOf(it)
                }

        return verbFormSolver
    }

    private fun generateVerbCaseSolver(): Map<Pair<Pair<TypedSpeechPart, Set<CategoryValue>>, SyntaxRelation/*TODO depend on speech part too*/>, CategoryValues> {
        val result: MutableMap<Pair<Pair<TypedSpeechPart, Set<CategoryValue>>, SyntaxRelation>, CategoryValues> =
            mutableMapOf()
        //TODO handle split
        val verbParadigms = changeParadigm.getSpeechPartParadigms(Verb)
        val cases = changeParadigm.categories.first { it.outType == caseName }.actualValues

        for (verbTypeParadigm in verbParadigms) {
            val verbType = verbTypeParadigm.speechPart
            val isTransitive = verbType.subtype == defaultSubtype
            val times = verbTypeParadigm.categories
                .firstOrNull { it.category is Tense }
                ?.category?.actualValues
                ?.map { setOf(it) }
                ?: listOf(setOf())

            for (time in times)
                if (CaseValue.Nominative in cases && CaseValue.Accusative in cases)
                    if (isTransitive) {
                        result[verbType to time to SyntaxRelation.Agent] = listOf(CaseValue.Nominative)
                        result[verbType to time to SyntaxRelation.Patient] = listOf(CaseValue.Accusative)
                    } else
                        result[verbType to time to SyntaxRelation.Argument] = listOf(CaseValue.Nominative)
                else if (CaseValue.Ergative in cases && CaseValue.Absolutive in cases)
                    if (isTransitive) {
                        result[verbType to time to SyntaxRelation.Agent] = listOf(CaseValue.Ergative)
                        result[verbType to time to SyntaxRelation.Patient] = listOf(CaseValue.Absolutive)
                    } else
                        result[verbType to time to SyntaxRelation.Argument] = listOf(CaseValue.Absolutive)
                else
                    if (isTransitive) {
                        result[verbType to time to SyntaxRelation.Agent] = listOf()
                        result[verbType to time to SyntaxRelation.Patient] = listOf()
                    } else
                        result[verbType to time to SyntaxRelation.Argument] = listOf()
        }

        return result
    }

    private fun generateVerbArgumentSolver(): Map<Pair<TypedSpeechPart, ObjectType>, SyntaxRelation> {
        val solver = mutableMapOf<Pair<TypedSpeechPart, ObjectType>, SyntaxRelation>()
        val possibleObliqueExperiencer = listOf(
            SyntaxRelation.Addressee.withProb(1.0),
            SyntaxRelation.Location.withProb(0.1)
        )

        for (verbType in changeParadigm.getSpeechParts(Verb)) {
            when (verbType.subtype) {
                in additionalVerbTypes.map { it.speechPart.subtype } -> {
                    solver[verbType to MainObjectType.Experiencer] = possibleObliqueExperiencer.randomUnwrappedElement()
                    solver[verbType to MainObjectType.Stimulus] = SyntaxRelation.Argument
                }
            }
        }

        return solver
    }

    private fun generateNumberCategorySolver() = changeParadigm.categories
        .filterIsInstance<Number>()
        .firstOrNull()
        ?.takeIf { it.actualValues.isNotEmpty() }
        ?.let { numberCategory ->
            val values = numberCategory.actualValues

            val paucalLowerBound = if (Dual in values) 3 else 2
            val paucalUpperBound = RandomSingleton.random.nextInt(paucalLowerBound + 1, paucalLowerBound + 9)
            val paucalBound = paucalLowerBound..paucalUpperBound

            val numberCategorySolver = values.map {
                it as NumberValue
                it to when (it) {
                    Singular -> 1..1
                    Dual -> 2..2
                    Paucal -> paucalBound
                    Plural -> 2..Int.MAX_VALUE
                }
            }.toMap().toMutableMap()

            if (Dual in values)
                numberCategorySolver[Plural] = 3..Int.MAX_VALUE
            else 0.05.chanceOf {
                numberCategorySolver[Plural] = 3..Int.MAX_VALUE
                numberCategorySolver[Singular] = 1..2
                if (Paucal in values)
                    numberCategorySolver[Paucal] = 3..paucalUpperBound
            }

            if (Paucal in values)
                numberCategorySolver[Plural] = (paucalBound.last + 1)..Int.MAX_VALUE

            val allForm =
                if (Plural in values) Plural
                else Singular

            NumberCategorySolver(numberCategorySolver, allForm)
        }

    private fun generateGenderCategorySolver() = changeParadigm.categories
        .filterIsInstance<NounClass>()
        .firstOrNull()
        ?.takeIf { it.actualValues.isNotEmpty() }
        ?.let { genderCategory ->
            val genderCategorySolver = genderCategory.actualValues.associate {
                it as NounClassValue
                it to it
            }.toMutableMap()

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
                .getCategoryValues(deixisName)
            val definitenessValues = changeParadigm.getSpeechPartParadigm(speechPart)
                .getCategoryValues(definitenessName)


            val indefiniteArticleWrapped = definitenessValues
                .firstOrNull { it == DefinitenessValue.Indefinite }
                ?.let { setOf(it) }
            val definiteArticleWrapped = definitenessValues
                .firstOrNull { it == DefinitenessValue.Definite }
                ?.let { setOf(it) }
            val definiteArticleOrUndefined = definiteArticleWrapped ?: setOf(Undefined)

            val naiveSolver = deixisValues.map {
                it as DeixisValue
                it to setOf<CategoryValue>(it)
            }.toMap().toMutableMap<DeixisValue?, Set<CategoryValue>>()

            if (deixisValues.isNotEmpty()) {
                naiveSolver[null] = setOf()

                val absentDeixis = changeParadigm.getSpeechPartParadigm(speechPart)
                    .getCategory(deixisName)
                    .category
                    .allPossibleValues
                    .filter { it !in deixisValues }
                    .map { it as DeixisValue }

                for (deixis in absentDeixis) naiveSolver[deixis] = when (deixis) {
                    Undefined -> definiteArticleWrapped ?: setOf()
                    Proximal -> definiteArticleOrUndefined
                    Medial -> deixisValues.randomPresent(Proximal, Distant) ?: definiteArticleOrUndefined
                    Distant -> definiteArticleOrUndefined
                    ProximalAddressee -> deixisValues.randomPresent(Proximal, Distant) ?: definiteArticleOrUndefined
                    Unseen -> deixisValues.randomPresent(Proximal, Distant) ?: definiteArticleOrUndefined
                    DistantHigher -> deixisValues.randomPresent(Distant) ?: definiteArticleOrUndefined
                    DistantLower -> deixisValues.randomPresent(Distant) ?: definiteArticleOrUndefined
                }
            }

            val definitenessNecessity = changeParadigm.getSpeechPartParadigm(speechPart)
                .getCategoryOrNull(definitenessName)
                ?.compulsoryData?.isCompulsory ?: false

            if (definitenessNecessity)
                for (deixis in DeixisValue::class.values() + listOf<DeixisValue?>(null)) when (deixis) {
                    null -> indefiniteArticleWrapped?.let {
                        naiveSolver[deixis] = (naiveSolver.getOrDefault(deixis, setOf()) + it).toSet()
                    }
                    else -> definiteArticleWrapped?.let {
                        naiveSolver[deixis] = (naiveSolver.getOrDefault(deixis, setOf()) + it).toSet()
                    }
                }

            for ((t, u) in naiveSolver)
                deixisCategorySolver[t to speechPart] = u.toList()
        }

        return deixisCategorySolver
    }

    private fun CategoryValues.randomPresent(vararg values: CategoryValue) = values.toList()
        .filter { it in this }.randomElementOrNull()
        ?.let { setOf(it) }

    private fun generatePersonalPronounDropSolver(): PersonalPronounDropSolver {
        val verbalCategories =
            changeParadigm.getSpeechPartParadigms(Verb).first().categories//TODO bullshit decision
        val pronounCategories =
            changeParadigm.getSpeechPartParadigm(PersonalPronoun.toDefault()).categories

        val personalPronounDropSolver = mutableListOf<Pair<SyntaxRelation, CategoryValues>>()

        for (actor in listOf(SyntaxRelation.Agent, SyntaxRelation.Argument, SyntaxRelation.Patient)) {
            val relevantCategories = verbalCategories
                .filter { it.source is CategorySource.Agreement && it.source.relation == actor }

            if (relevantCategories.size == pronounCategories.size) 0.5.chanceOf {
                listCartesianProduct(pronounCategories.map { it.category.actualValues })
                    .forEach { personalPronounDropSolver += actor to it }
            } else if (relevantCategories.isNotEmpty()) 0.5.chanceOf {
                listCartesianProduct(pronounCategories.map { it.category.actualValues })
                    .randomElement()
                    .let { personalPronounDropSolver += actor to it }
            }
        }

        return personalPronounDropSolver
    }
}
