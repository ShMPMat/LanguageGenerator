package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.Category
import io.tashtabash.lang.language.category.CategorySource.*
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.sequence.unfold
import io.tashtabash.lang.utils.listCartesianProduct


data class WordChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<TypedSpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(
        word: Word,
        latchType: LatchType = LatchType.Center,
        categoryValues: List<SourcedCategoryValue> = getDefaultState(word)
    ): WordClauseResult {
        val simpleCategoryValues = categoryValues.map { it.categoryValue }
        val applicableValues = categoryValues
            .filter { it.parent.compulsoryData.isApplicable(simpleCategoryValues) }
            .toSet()

        return applyToNewWords(
            speechPartChangeParadigms[word.semanticsCore.speechPart]
                ?.apply(word, latchType, applicableValues)
                ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}"),
            applicableValues
        )
    }

    private fun applyToNewWords(
        wordPair: Pair<FoldedWordSequence, Int>,
        values: Set<SourcedCategoryValue>
    ): WordClauseResult {
        val (ws, i) = wordPair

        val newWs = ws.words.flatMapIndexed { j, (w, l) ->
            if (!isAlreadyProcessed(w, j, i)) {
                applyToNewWord(w, l, values)
            } else listOf(w to l)
        }
        return WordClauseResult(FoldedWordSequence(newWs), i)
    }

    private fun applyToNewWord(
        word: Word,
        latchType: LatchType,
        values: Set<SourcedCategoryValue>
    ): List<Pair<Word, LatchType>> {
        val isAdnominal = word.semanticsCore.speechPart.subtype == adnominalSubtype
        val isArticle = word.semanticsCore.speechPart.type == SpeechPart.Article
        return if (isAdnominal || isArticle) {
            val newCv = values.map {
                SourcedCategoryValue(
                    it.categoryValue,
                    Agreement(SyntaxRelation.Agent, nominals),
                    it.parent
                )
            }
            apply(word, latchType, newCv).words.words
        } else
            listOf(word to latchType)
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
            .union(word.semanticsCore.staticCategories.map { v ->
                val value = paradigm.categories.first { it.category.outType == it.category.outType }

                SourcedCategoryValue(v, Self, value)
            })
            .toList()
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

    fun getAllWordForms(word: Word, includeOptionalCategories: Boolean): List<Pair<WordSequence, SourcedCategoryValues>> =
        getAllCategoryValueCombinations(word.semanticsCore.speechPart, includeOptionalCategories)
            .map { apply(word, categoryValues = it).unfold() to it }

    val speechParts = speechPartChangeParadigms.keys.sortedBy { it.type }

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n\n")
}
