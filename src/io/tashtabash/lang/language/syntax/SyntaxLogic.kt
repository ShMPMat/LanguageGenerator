package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.*
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import io.tashtabash.lang.language.syntax.clause.construction.VerbConstruction
import io.tashtabash.lang.language.syntax.clause.description.ObjectType
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.context.DescriptionContext
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import io.tashtabash.lang.language.syntax.context.Priority
import io.tashtabash.lang.language.syntax.transformer.SyntaxNodeMatcher
import io.tashtabash.lang.language.syntax.transformer.Transformer
import kotlin.math.abs


data class SyntaxLogic(
    // Will be applied to Aux if it's present
    private val verbFormSolver: Map<VerbContextInfo, SourcedCategoryValues> = mapOf(),
    // Maps Description-level semantic roles to Clause-level syntactic relations
    private val verbArgumentSolver: Map<Pair<TypedSpeechPart, ObjectType>, SyntaxRelation> = mapOf(),
    // Maps Clause-level syntactic relations to specific category values (cases/adpositions)
    private val verbCasesSolver: Map<Pair<TypedSpeechPart, SyntaxRelation>, CategoryValues> = mapOf(),
    private val copulaCaseSolver: Map<Pair<Pair<CopulaConstruction, SyntaxRelation>, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val syntaxRelationSolver: Map<Pair<SyntaxRelation, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val numberCategorySolver: NumberCategorySolver? = null,
    private val nounClassCategorySolver: Map<NounClassValue, NounClassValue>? = null,
    private val deixisDefinitenessCategorySolver: Map<Pair<DeixisValue?, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val personalPronounInclusivity: SourcedCategory? = null, // WALS only knows about separate inclusive
    val transformers: List<Pair<SyntaxNodeMatcher, Transformer>> = listOf(),
    val verbConstructions: Map<VerbContextInfo, VerbConstruction> = mapOf(),
) {
    fun resolvePronounCategories(actorValue: ActorValue, speechPart: TypedSpeechPart): CategoryValues {
        val resultCategories = mutableListOf<CategoryValue>()
        val (person, gender, amount, deixis, inclusivity) =
            actorValue

        resultCategories += person
        resultCategories += deixisDefinitenessCategorySolver.getOrDefault(deixis to speechPart, listOf())
        resultCategories.addNumber(amount)

        if (nounClassCategorySolver != null)
            resultCategories += nounClassCategorySolver.getValue(gender)

        inclusivity?.let { resultCategories += it } ?: run {
            if (personalPronounInclusivity?.compulsoryData?.isApplicable(resultCategories) == true) {
                resultCategories += InclusivityValue.Exclusive
            }
        }

        return resultCategories
    }

    fun resolveComplimentCategories(
        actorCompliment: ActorComplimentValue,
        speechPart: TypedSpeechPart
    ): CategoryValues {
        val resultCategories = mutableListOf<CategoryValue>()
        val (amount, deixis) = actorCompliment

        resultCategories += deixisDefinitenessCategorySolver.getOrDefault(deixis to speechPart, listOf())
        resultCategories.addNumber(amount)

        return resultCategories
    }

    private fun MutableList<CategoryValue>.addNumber(amount: Amount) {
        if (numberCategorySolver != null) {
            val number = when (amount) {
                is Amount.All -> numberCategorySolver.allForm
                is Amount.AmountValue -> numberCategorySolver.amountMap.entries
                    .firstOrNull { it.value.contains(amount.amount) }
                    ?.key
                    ?: throw LanguageException("No handler for amount ${amount.amount}")
            }
            add(number)
        }
    }

    fun resolveVerbCase(verbType: TypedSpeechPart, syntaxRelation: SyntaxRelation): CategoryValues =
        verbCasesSolver.getValue(verbType to syntaxRelation)

    fun resolveCopulaCase(
        copulaType: CopulaConstruction,
        syntaxRelation: SyntaxRelation,
        speechPart: TypedSpeechPart
    ): CategoryValues =
        copulaCaseSolver.getValue(copulaType to syntaxRelation to speechPart)

    fun resolveSyntaxRelationToCase(syntaxRelation: SyntaxRelation, speechPart: TypedSpeechPart): CategoryValues =
        syntaxRelationSolver.getValue(syntaxRelation to speechPart)

    fun resolveVerbForm(language: Language, verbType: TypedSpeechPart, context: DescriptionContext): List<SourcedCategoryValue> =
        resolveTime(language, verbType, context) +
                language.changeParadigm.wordChangeParadigm
                    .getParadigm(verbType)
                    .getValueOrEmpty(moodName, MoodValue.Indicative)

    fun resolveVerbConstruction(verbType: TypedSpeechPart, context: DescriptionContext): VerbConstruction? =
        verbConstructions[verbType to context.time.first]

    fun resolveAdjectiveForm(language: Language, adjectiveType: TypedSpeechPart, context: DescriptionContext) =
        resolveTime(language, adjectiveType, context)

    fun applyTransformers(node: SyntaxNode) {
        for ((condition, transformer) in transformers)
            if (condition.match(node))
                transformer.apply(node, this)

        for ((_, child) in node.children)
            applyTransformers(child)
    }

    private fun resolveTime(language: Language, speechPart: TypedSpeechPart, context: DescriptionContext): SourcedCategoryValues {
        val (timeValue, priority) = context.time

        verbFormSolver[speechPart to timeValue]?.let { categories ->
            return categories.map { it }
        }

        if (priority == Priority.Explicit) {
            TODO()
        } else
            return chooseClosestTense(language.changeParadigm.wordChangeParadigm.getParadigm(speechPart), timeValue)
    }

    fun resolveArgumentTypes(speechPart: TypedSpeechPart, objectType: ObjectType): SyntaxRelation =
        verbArgumentSolver.getOrDefault(speechPart to objectType, objectType.relation)

    fun resolvePossibleArguments(speechPart: TypedSpeechPart): List<SyntaxRelation> =
        verbArgumentSolver.filter { (k) -> k.first == speechPart }
            .map { it.value }

    override fun toString() = """
        |Syntax:
        |
        |${
        verbFormSolver.map { (context, categories) ->
            listOf(
                "For ${context.first}, ",
                "${context.second} ",
                " used tense is: ",
                categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |${
        verbCasesSolver.map { (context, categories) ->
            listOf(
                "${context.first}, ",
                "${context.second} ",
                " used cases are: ",
                categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |Verb governance:
        |${
        verbArgumentSolver.map { (context, objectType) ->
            listOf(
                "${context.first} ",
                "governs ${context.second} ",
                "as ",
                objectType.toString()
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |${
        numberCategorySolver?.amountMap?.map { (number, range) ->
            listOf("$number ", "is used for amounts $range")
        }
            ?.lineUpAll()
            ?.joinToString("\n")
            ?: ""
    }
        |All instances - ${numberCategorySolver?.allForm}
        |
        |${
        nounClassCategorySolver?.map { (g1, g2) ->
            listOf("$g1", " is seen as $g2")
        }
            ?.lineUpAll()
            ?.joinToString("\n")
            ?: ""
    }
        | 
        |${
        deixisDefinitenessCategorySolver.map { (g1, g2) ->
            listOf("${g1.first}, ", "${g1.second} ", " is expressed as $g2")
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |${
        copulaCaseSolver.map { (context, categories) ->
            listOf(
                "For ${context.first.first}, ",
                "${context.first.second}, ",
                "${context.second} ",
                " the following cases are used: " + categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |${
        syntaxRelationSolver.map { (context, categories) ->
            listOf(
                "For ${context.first}, ",
                "${context.second} ",
                " used cases are: " + categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |Analytic constructions:
        |${
        verbConstructions.map { (context, construction) ->
            listOf(
                "For ${context.first}, ",
                "${context.second} ",
                " use: $construction"
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |Additional rules:
        |${
            transformers.joinToString("\n") { (condition, change) ->
                "If a node $condition, then $change"
            }
    }
    """.trimMargin()
}


fun chooseClosestTense(changeParadigm: SpeechPartChangeParadigm, timeContext: TimeContext): SourcedCategoryValues {
    val timeValueNumber = timeContext.toNumber()

    val tense = changeParadigm.categories
        .firstOrNull { it.category is Tense }
        ?.actualSourcedValues
        ?.minByOrNull {
            val tenseNumber = (it.categoryValue as TenseValue).toNumber()

            abs(tenseNumber - timeValueNumber)
        }
        ?: return emptyList()

    return listOf(tense)
}

private fun TenseValue.toNumber() = when (this) {
    TenseValue.Present -> 0
    TenseValue.Future -> 10
    TenseValue.Past -> -100
    TenseValue.DayPast -> -60
    TenseValue.SomeDaysPast -> -70
    TenseValue.MonthPast -> -80
    TenseValue.YearPast -> -90
}

private fun TimeContext.toNumber() = when (this) {
    TimeContext.Present -> .0
    TimeContext.ImmediateFuture -> 7.0
    TimeContext.ImmediatePast -> -97.0
    TimeContext.Future -> 10.0
    TimeContext.FarFuture -> 20.0
    TimeContext.Past -> -100.0
    TimeContext.DayPast -> -60.0
    TimeContext.SomeDaysPast -> -70.0
    TimeContext.MonthPast -> -80.0
    TimeContext.YearPast -> -90.0
    TimeContext.LongGonePast -> -1000.0
    TimeContext.Regular -> Double.NaN
}

data class NumberCategorySolver(val amountMap: Map<NumberValue, IntRange>, val allForm: NumberValue)

typealias VerbContextInfo = Pair<TypedSpeechPart, TimeContext>