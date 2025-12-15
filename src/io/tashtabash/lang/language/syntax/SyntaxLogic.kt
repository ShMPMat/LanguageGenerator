package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.*
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.syntax.clause.description.ObjectType
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import io.tashtabash.lang.language.syntax.context.Priority
import io.tashtabash.lang.language.syntax.features.CopulaType
import io.tashtabash.lang.language.syntax.transformer.SyntaxNodeMatcher
import io.tashtabash.lang.language.syntax.transformer.Transformer
import kotlin.math.abs


data class SyntaxLogic(
    private val timeFormSolver: Map<VerbContextInfo, SourcedCategoryValues> = mapOf(),
    // Maps Description-level semantic roles to Clause-level syntactic relations
    private val verbArgumentSolver: Map<Pair<TypedSpeechPart, ObjectType>, SyntaxRelation> = mapOf(),
    // Maps Clause-level syntactic relations to specific category values (cases/adpositions)
    private val verbCasesSolver: Map<Pair<Pair<TypedSpeechPart, Set<CategoryValue>>, SyntaxRelation>, CategoryValues> = mapOf(),
    private val copulaCaseSolver: Map<Pair<Pair<CopulaType, SyntaxRelation>, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val syntaxRelationSolver: Map<Pair<SyntaxRelation, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val numberCategorySolver: NumberCategorySolver? = null,
    private val nounClassCategorySolver: Map<NounClassValue, NounClassValue>? = null,
    private val deixisDefinitenessCategorySolver: Map<Pair<DeixisValue?, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val personalPronounInclusivity: SourcedCategory? = null, // WALS only knows about separate inclusive
    private val transformers: List<Pair<SyntaxNodeMatcher, Transformer>> = listOf()
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

    fun resolveVerbCase(
        verbType: TypedSpeechPart,
        syntaxRelation: SyntaxRelation,
        categories: Set<CategoryValue>
    ): CategoryValues =
        verbCasesSolver.getValue(verbType to categories to syntaxRelation)

    fun resolveCopulaCase(
        copulaType: CopulaType,
        syntaxRelation: SyntaxRelation,
        speechPart: TypedSpeechPart
    ): CategoryValues =
        copulaCaseSolver.getValue(copulaType to syntaxRelation to speechPart)

    fun resolveSyntaxRelationToCase(syntaxRelation: SyntaxRelation, speechPart: TypedSpeechPart): CategoryValues =
        syntaxRelationSolver.getValue(syntaxRelation to speechPart)

    fun resolveVerbForm(language: Language, verbType: TypedSpeechPart, context: Context) =
        resolveTime(language, verbType, context)

    fun resolveAdjectiveForm(language: Language, adjectiveType: TypedSpeechPart, context: Context) =
        resolveTime(language, adjectiveType, context)

    fun applyTransformers(node: SyntaxNode) {
        for ((condition, transformer) in transformers)
            if (condition.match(node))
                transformer.apply(node)

        for ((_, child) in node.children)
            applyTransformers(child)
    }

    private fun resolveTime(language: Language, speechPart: TypedSpeechPart, context: Context): SourcedCategoryValues {
        val (timeValue, priority) = context.time

        timeFormSolver[speechPart to timeValue]?.let { categories ->
            return categories.map { it }
        }

        if (priority == Priority.Explicit) {
            TODO()
        } else
            return chooseClosestTense(language, speechPart, timeValue)
    }

    fun resolveArgumentTypes(speechPart: TypedSpeechPart, objectType: ObjectType): SyntaxRelation =
        verbArgumentSolver.getOrDefault(speechPart to objectType, objectType.relation)

    fun resolvePossibleArguments(speechPart: TypedSpeechPart): List<SyntaxRelation> =
        verbArgumentSolver.filter { (k) -> k.first == speechPart }
            .map { it.value }

    private fun chooseClosestTense(
        language: Language,
        verbType: TypedSpeechPart,
        timeContext: TimeContext
    ): SourcedCategoryValues {
        val timeValueNumber = timeContext.toNumber()

        val tense = language.changeParadigm.wordChangeParadigm
            .getSpeechPartParadigm(verbType)
            .categories
            .firstOrNull { it.category is Tense }
            ?.actualSourcedValues
            ?.minByOrNull {
                val tenseNumber = (it.categoryValue as TenseValue).toNumber()

                abs(tenseNumber - timeValueNumber)
            }
            ?: return emptyList()

        return listOf(tense)
    }

    override fun toString() = """
        |Syntax:
        |
        |${
        timeFormSolver.map { (context, categories) ->
            listOf(
                "For ${context.first}, ",
                "${context.second} ",
                " the following form is used: ",
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
                "For ${context.first.first}, ",
                "${context.first.second}, ",
                "${context.second} ",
                " the following cases are used: ",
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
                " the following cases are used: " + categories.joinToString(", ")
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