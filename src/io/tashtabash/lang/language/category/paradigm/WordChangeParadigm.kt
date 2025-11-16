package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.generator.ValueMap
import io.tashtabash.lang.language.category.Category
import io.tashtabash.lang.language.category.CategorySource.*
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.category.realization.PassingCategoryApplicator
import io.tashtabash.lang.language.category.realization.WordCategoryApplicator
import io.tashtabash.lang.language.category.realization.analyticalRealizations
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.PhonologicalRuleApplicator
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.*
import io.tashtabash.lang.utils.cartesianProduct
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking


data class WordChangeParadigm(
    val categories: List<Category>,
    val speechPartChangeParadigms: Map<TypedSpeechPart, SpeechPartChangeParadigm>,
    val sandhiRules: List<PhonologicalRule> = listOf()
) {
    fun hasPrefixes(): Boolean =
        speechPartChangeParadigms.values
            .any { it.hasPrefixes() }

    fun hasSuffixes(): Boolean =
        speechPartChangeParadigms.values
            .any { it.hasSuffixes() }

    fun apply(
        word: Word,
        latchType: LatchType = LatchType.Center,
        categoryValues: List<SourcedCategoryValue> = getDefaultState(word)
    ): WordClauseResult {
        val simpleCategoryValues = categoryValues.map { it.categoryValue }
        val applicableValues = categoryValues
            .filter { it.parent.compulsoryData.isApplicable(simpleCategoryValues) }
            .toSet()

        val startingClause = speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?.apply(word, latchType, applicableValues)
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")
        val resultingClause = applyToNewWords(
            startingClause,
            applicableValues + getStaticCategoryValues(word)
        )

        return resultingClause.map { applySandhiRules(it) }
    }

    private fun applyToNewWords(wordClause: WordClauseResult, values: Set<SourcedCategoryValue>): WordClauseResult {
        val (ws, i) = wordClause

        val newWs = ws.words.flatMapIndexed { j, (w, l) ->
            if (isAlreadyProcessed(w, j, i, wordClause.mainWord))
                listOf(LatchedWord(w, l))
            else {
                val convertedValues = convertValuesForDependentClause(
                    values,
                    wordClause.mainWord.semanticsCore.speechPart.type
                )
                apply(w, l, convertedValues)
                    .words
                    .words
            }
        }
        return WordClauseResult(FoldedWordSequence(newWs), i)
    }

    private fun convertValuesForDependentClause(
        values: Set<SourcedCategoryValue>,
        rootSpeechPart: SpeechPart
    ): SourcedCategoryValues = when (rootSpeechPart) {
        SpeechPart.Noun -> values.map {
            it.copy(source = Agreement(SyntaxRelation.Agent, nominals))
        }
        else -> throw ChangeException("Can't convert category values for a dependent clause of $rootSpeechPart")
    }

    private fun isAlreadyProcessed(word: Word, curIdx: Int, mainWordIdx: Int, mainWord: Word) =
        mainWordIdx == curIdx
                || !isChangeable(word)
                || word.semanticsCore == mainWord.semanticsCore

    fun isChangeable(word: Word): Boolean =
        word.semanticsCore.speechPart.type !in listOf(SpeechPart.Particle, SpeechPart.Adposition)

    internal fun getDefaultState(word: Word): List<SourcedCategoryValue> {
        val paradigm = speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

        return paradigm.exponenceClusters
            .flatMap { it.categories }
            .filter { it.actualSourcedValues.isNotEmpty() && it.compulsoryData.isCompulsory }
            .map { it.actualSourcedValues[0] }//TODO another method for static categories swap
            .filter { v ->
                word.semanticsCore.staticCategories.none { it.parentClassName == v.categoryValue.parentClassName }
            }
            .union(getStaticCategoryValues(word))
            .toList()
    }

    private fun getStaticCategoryValues(word: Word): List<SourcedCategoryValue> =
        word.semanticsCore
            .staticCategories
            .mapNotNull {
                val parentCategory = speechPartChangeParadigms[word.semanticsCore.speechPart]
                    ?.getCategoryOrNull(it.parentClassName)
                    ?: return@mapNotNull null

                parentCategory[it]
            }

    fun getSpeechPartParadigm(speechPart: TypedSpeechPart) =
        speechPartChangeParadigms[speechPart]
            ?: throw ChangeException("The change paradigm for '$speechPart' doesn't exist")

    fun getSpeechPartParadigms(speechPart: SpeechPart) = speechPartChangeParadigms
        .entries.filter { it.key.type == speechPart }
        .map { it.value }

    fun getSpeechParts(speechPart: SpeechPart) = speechPartChangeParadigms
        .keys.filter { it.type == speechPart }

    fun getAllCategoryValueCombinations(
        speechPart: TypedSpeechPart,
        includeOptionalCategories: Boolean,
    ): List<SourcedCategoryValues> {
        val compulsoryCategoryProduct = getSpeechPartParadigm(speechPart)
            .categories
            .filter { it.compulsoryData.isCompulsory }
            .filter { speechPart.type !in it.category.staticSpeechParts }
            .map { it.actualSourcedValues }
            .cartesianProduct()

        if (!includeOptionalCategories)
            return compulsoryCategoryProduct

        val optionalCategoryProduct = compulsoryCategoryProduct.toMutableList()
        val optionalCategories = getSpeechPartParadigm(speechPart)
            .categories
            .filter { !it.compulsoryData.isCompulsory }
            .filter { speechPart.type !in it.category.staticSpeechParts }
        for (optionalCategory in optionalCategories)
            optionalCategoryProduct += optionalCategory.actualSourcedValues
                .flatMap { v -> optionalCategoryProduct.map { it + v } }

        return optionalCategoryProduct
    }

    // Includes compulsory analytically expressed categories, but minimizes them
    private fun getAllSyntheticCategoryValueCombinations(word: Word): List<SourcedCategoryValues> =
        getSpeechPartParadigm(word.semanticsCore.speechPart)
            .applicators.filter { (cluster, valueMap) -> cluster.isCompulsory || !valueMap.isAnalytical }
            .entries.map { (_, valueMap) ->
                val analyticClusterValues: List<List<SourcedCategoryValue>> = valueMap.entries
                    .filter { it.value.type !in analyticalRealizations }
                    .map { it.key.categoryValues } +
                        // Add one of the passing matchers if exists
                        listOfNotNull(
                            valueMap.entries
                                .firstOrNull { it.value == PassingCategoryApplicator }
                                ?.key
                                ?.categoryValues
                        )
                val chosenClusterValues = analyticClusterValues.takeIf { it.isNotEmpty() }
                    ?: listOf(valueMap.keys.first().categoryValues)

                // Filter out values which can't be applied to the word's static categories
                val wordStaticCategories = word.semanticsCore.staticCategories.map { it.parentClassName }
                val wordRelevantClusterValues = chosenClusterValues.filter { values ->
                    val valuesFromStaticCategories = values
                        .filter { it.source == Self && it.categoryValue.parentClassName in wordStaticCategories }
                        .map { it.categoryValue }

                    valuesFromStaticCategories.isEmpty()
                            || word.semanticsCore.staticCategories.all { it in valuesFromStaticCategories }
                }

                if (wordRelevantClusterValues.isNotEmpty())
                    wordRelevantClusterValues.map { categoryValues ->
                        categoryValues.filter { word.semanticsCore.speechPart.type !in it.parent.category.staticSpeechParts }
                    }
                else listOf(listOf()) // Return one element of empty values representing "zero marking"
            }.cartesianProduct()
            // Unite separate exponence clusters
            .map { it.flatten() }

    fun getAllWordForms(
        word: Word,
        includeOptionalCategories: Boolean
    ): List<Pair<WordSequence, SourcedCategoryValues>> = runBlocking {
        getAllCategoryValueCombinations(word.semanticsCore.speechPart, includeOptionalCategories)
            .map {
                async {
                    apply(word, categoryValues = it).unfold() to it
                }
            }.awaitAll()
    }

    fun getAllWordForms(
        lexis: Lexis,
        includeOptionalCategories: Boolean
    ): List<Pair<WordSequence, SourcedCategoryValues>> = runBlocking {
        lexis.words
            .map {
                async {
                    getAllWordForms(it, includeOptionalCategories)
                }
            }.awaitAll()
            .flatten()
    }

    fun getUniqueWordForms(word: Word): List<Deferred<Word>> = runBlocking {
        getAllSyntheticCategoryValueCombinations(word)
            .map {
                async {
                    apply(word, categoryValues = it).mainWord
                }
            }
    }

    // May contain duplicates
    fun getUniqueWordForms(lexis: Lexis): List<Word> = runBlocking {
        val contentWords = lexis.words
            .map {
                async {
                    getUniqueWordForms(it)
                }
            }
        val functionWords = speechPartChangeParadigms.values
            .flatMap { p ->
                p.applicators
                    .values
                    .flatMap { it.values }
            }
            .filterIsInstance<WordCategoryApplicator>()
            .map {
                async {
                    getUniqueWordForms(it.word)
                }
            }

        (contentWords + functionWords).awaitAll()
            .flatten()
            .awaitAll()
    }

    val speechParts = speechPartChangeParadigms.keys.sortedBy { it.type }

    fun mapApplicators(mapper: (CategoryApplicator) -> CategoryApplicator) = WordChangeParadigm(
        categories,
        speechPartChangeParadigms.mapValues { (_, speechPartChangeParadigm) ->
            val mappedApplicators = speechPartChangeParadigm.applicators
                .mapValues { (_, exponenceToApplicator) ->
                    ValueMap(
                        exponenceToApplicator.mapValues { (_, applicator) ->
                            mapper(applicator)
                        }
                    )
                }

            speechPartChangeParadigm.copy(applicators = mappedApplicators)
        },
        sandhiRules
    )

    private fun applySandhiRules(word: Word): Word {
        val phonologicalRuleApplicator = PhonologicalRuleApplicator()

        return sandhiRules.fold(word) { curWord, rule ->
            // Don't change word's history, it's not needed for word forms
            phonologicalRuleApplicator.applyPhonologicalRule(curWord, rule, false)
        }
    }

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            sandhiRules.joinToString("\n", "Sandhi rules:\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n\n")
}
