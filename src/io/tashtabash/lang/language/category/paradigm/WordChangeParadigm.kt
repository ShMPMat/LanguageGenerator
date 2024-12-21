package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.Category
import io.tashtabash.lang.language.category.CategorySource.*
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.diachronicity.PhonologicalRule
import io.tashtabash.lang.language.diachronicity.PhonologicalRuleApplicator
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.*
import io.tashtabash.lang.utils.listCartesianProduct


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

        val resultingClause = applyToNewWords(
            speechPartChangeParadigms[word.semanticsCore.speechPart]
                ?.apply(word, latchType, applicableValues)
                ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}"),
            applicableValues + getStaticCategoryValues(word)
        )

        return resultingClause.map { applySandhiRules(it) }
    }

    private fun applyToNewWords(
        wordClause: WordClauseResult,
        values: Set<SourcedCategoryValue>
    ): WordClauseResult {
        val (ws, i) = wordClause

        val newWs = ws.words.flatMapIndexed { j, (w, l) ->
            if (!isAlreadyProcessed(w, j, i)) {
                applyToNewWord(w, l, values)
            } else listOf(LatchedWord(w, l))
        }
        return WordClauseResult(FoldedWordSequence(newWs), i)
    }

    private fun applyToNewWord(
        word: Word,
        latchType: LatchType,
        values: Set<SourcedCategoryValue>
    ): List<LatchedWord> {
        val isAdnominal = word.semanticsCore.speechPart.subtype == adnominalSubtype
        val isArticle = word.semanticsCore.speechPart.type == SpeechPart.Article
        return if (isAdnominal || isArticle) {
            val newCv = values.map {
                it.copy(source = Agreement(SyntaxRelation.Agent, nominals))
            }
            apply(word, latchType, newCv).words.words
        } else
            listOf(LatchedWord(word, latchType))
    }

    private fun isAlreadyProcessed(word: Word, curIdx: Int, mainWordIdx: Int) =
        mainWordIdx == curIdx || word.semanticsCore.speechPart.type == SpeechPart.Particle

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

                SourcedCategoryValue(it, Self, parentCategory)
            }

    fun getSpeechPartParadigm(speechPart: TypedSpeechPart) = speechPartChangeParadigms.getValue(speechPart)
    fun getSpeechPartParadigms(speechPart: SpeechPart) = speechPartChangeParadigms
        .entries.filter { it.key.type == speechPart }
        .map { it.value }

    fun getSpeechParts(speechPart: SpeechPart) = speechPartChangeParadigms
        .keys.filter { it.type == speechPart }

    fun getAllCategoryValueCombinations(
        speechPart: TypedSpeechPart,
        includeOptionalCategories: Boolean,
    ): List<SourcedCategoryValues> =
        listCartesianProduct(
            getSpeechPartParadigm(speechPart)
                .categories
                .filter { if (includeOptionalCategories) true else it.compulsoryData.isCompulsory }
                .filter { !it.category.staticSpeechParts.contains(speechPart.type) }
                .map { it.actualSourcedValues }
        )

    fun getAllWordForms(
        word: Word,
        includeOptionalCategories: Boolean
    ): List<Pair<WordSequence, SourcedCategoryValues>> =
        getAllCategoryValueCombinations(word.semanticsCore.speechPart, includeOptionalCategories)
            .map { apply(word, categoryValues = it).unfold() to it }

    val speechParts = speechPartChangeParadigms.keys.sortedBy { it.type }

    fun mapApplicators(mapper: (CategoryApplicator) -> CategoryApplicator) = WordChangeParadigm(
        categories,
        speechPartChangeParadigms.mapValues { (_, speechPartChangeParadigm) ->
            val mappedApplicators = speechPartChangeParadigm.applicators
                .mapValues { (_, exponenceToApplicator) ->
                    exponenceToApplicator.mapValues { (_, applicator) ->
                        mapper(applicator)
                    }
                }

            speechPartChangeParadigm.copy(applicators = mappedApplicators)
        },
        sandhiRules
    )

    private fun applySandhiRules(word: Word): Word {
        val phonologicalRuleApplicator = PhonologicalRuleApplicator()

        return sandhiRules.fold(word) { curWord, rule ->
            phonologicalRuleApplicator.applyPhonologicalRule(curWord, rule)
        }
    }

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            sandhiRules.joinToString("\n", "Sandhi rules:\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n\n")
}
