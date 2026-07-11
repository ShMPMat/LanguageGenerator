package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.LanguageException
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking


data class WordChangeParadigm(
    val categories: List<Category<*>>,
    val speechPartChangeParadigms: Map<TypedSpeechPart, SpeechPartChangeParadigm>,
    val sandhiRules: List<PhonologicalRule> = listOf()
) {
    fun hasPrefixes(): Boolean =
        speechPartChangeParadigms.values
            .any { it.hasPrefixes() }

    fun hasSuffixes(): Boolean =
        speechPartChangeParadigms.values
            .any { it.hasSuffixes() }

    operator fun get(categoryName: String): Category<*> =
        categories.first { it.outType == categoryName }

    fun apply(
        word: Word,
        categoryValues: List<SourcedCategoryValue> = getDefaultState(word),
        latchType: LatchType = LatchType.Center
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
                    wordClause.mainWord?.semanticsCore?.speechPart?.type
                )
                apply(w, convertedValues, l)
                    .words
                    .words
            }
        }
        return WordClauseResult(FoldedWordSequence(newWs), i)
    }

    private fun convertValuesForDependentClause(
        values: Set<SourcedCategoryValue>,
        rootSpeechPart: SpeechPart?
    ): SourcedCategoryValues = when (rootSpeechPart) {
        SpeechPart.Noun -> values.map {
            it.copy(source = Agreement(SyntaxRelation.Agent, nominals))
        }
        else -> throw ChangeException("Can't convert category values for a dependent clause of $rootSpeechPart")
    }

    private fun isAlreadyProcessed(word: Word, curIdx: Int, mainWordIdx: Int?, mainWord: Word?) =
        mainWordIdx == curIdx
                || !isChangeable(word)
                || word.semanticsCore == mainWord?.semanticsCore // Don't process reduplicated words

    fun isChangeable(word: Word): Boolean =
        word.semanticsCore.speechPart.type !in listOf(SpeechPart.Particle, SpeechPart.Adposition)

    internal fun getDefaultState(word: Word): List<SourcedCategoryValue> {
        val paradigm = speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

        return paradigm.categories
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
                    ?.get(it.parentClassName)
                    ?: return@mapNotNull null

                parentCategory[it]
            }

    fun getParadigm(speechPart: TypedSpeechPart): SpeechPartChangeParadigm =
        speechPartChangeParadigms[speechPart]
            ?: throw ChangeException("The change paradigm for '$speechPart' doesn't exist")

    fun getParadigms(speechPart: SpeechPart) = speechPartChangeParadigms
        .entries.filter { it.key.type == speechPart }
        .map { it.value }

    fun getSpeechParts(speechPart: SpeechPart) = speechPartChangeParadigms
        .keys.filter { it.type == speechPart }

    fun getAllCategoryValueCombinations(
        speechPart: TypedSpeechPart,
        includeOptionalCategories: Boolean,
    ): List<SourcedCategoryValues> {
        val compulsoryCategoryProduct = getParadigm(speechPart)
            .categories
            .filter { it.compulsoryData.isCompulsory }
            .filter { speechPart.type !in it.category.staticSpeechParts }
            .map { it.actualSourcedValues }
            .cartesianProduct()

        if (!includeOptionalCategories)
            return compulsoryCategoryProduct

        val optionalCategoryProduct = compulsoryCategoryProduct.toMutableList()
        val optionalCategories = getParadigm(speechPart)
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
        getParadigm(word.semanticsCore.speechPart)
            .applicators.mapNotNull { (cluster, source) ->
                val target =
                    if (source is LinkCategoryHandler)
                        source.target
                    else source

                if (target !is SyntheticCategoryHandler)
                    return@mapNotNull null
                else if (cluster.isCompulsory || !target.applicatorSource.map.isAnalytical)
                    target.applicatorSource
                else null
            }
            .map { applicatorSource ->
                val analyticClusterValues: List<List<SourcedCategoryValue>> = applicatorSource.map.entries
                    .filter { it.value.type !in analyticalRealizations }
                    .map { it.key.categoryValues } +
                        // Add one of the passing matchers if exists
                        listOfNotNull(
                            applicatorSource.map.entries
                                .firstOrNull { it.value == PassingCategoryApplicator }
                                ?.key
                                ?.categoryValues
                        )
                val chosenClusterValues = analyticClusterValues.takeIf { it.isNotEmpty() }
                    ?: listOf(applicatorSource.map.keys.first().categoryValues)

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
                    apply(word, it).unfold() to it
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

    suspend fun getUniqueWordForms(word: Word): List<Deferred<List<Word>>> = coroutineScope {
        getAllSyntheticCategoryValueCombinations(word)
            .map {
                async {
                    val result = apply(word, it)
                    if (result.mainWord != null)
                        listOf(result.mainWord)
                    else result.words.words.map { w -> w.word }
                }
            }
    }

    // May contain duplicates
    fun getUniqueWordForms(lexis: Lexis): List<List<Word>> = runBlocking(Dispatchers.Default) {
        val contentWords: List<Deferred<List<Deferred<List<Word>>>>> = lexis.words
            .map { word: Word ->
                async {
                    getUniqueWordForms(word)
                }
            }
        val functionWords = speechPartChangeParadigms.values
            .flatMap { p ->
                p.sources
                    .filterIsInstance<SyntheticCategoryHandler>()
                    .flatMap { it.applicatorSource.map.values }
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
                .map { (exponenceCluster, source) ->
                    exponenceCluster to when (source) {
                        is SyntheticCategoryHandler -> SyntheticCategoryHandler(
                            source.applicatorSource.mapApplicators { _, applicator ->
                                mapper(applicator)
                            }
                        )
                        // Don't map anything, if the change is global, the map will be mapped in the source
                        is LinkCategoryHandler -> source
                        else -> throw LanguageException("Unexpected source type ${source::class}")
                    }
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
