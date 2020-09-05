package shmp.language.category.paradigm

import shmp.language.SpeechPart
import shmp.language.lexis.Word
import shmp.language.category.Category
import shmp.language.category.CategorySource
import shmp.language.syntax.WordSequence

class WordChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryValues: List<ParametrizedCategoryValue> = getDefaultState(word)): WordSequence {
        val (startClause, wordPosition) = innerApply(word, categoryValues)
        val allWords = startClause.words.mapIndexed { i, w ->
            if (i == wordPosition || w == startClause[i]) listOf(w)
            else apply(w, categoryValues).words
        }.flatten()
        return WordSequence(allWords)
    }

    private fun innerApply(
        word: Word,
        categoryValues: List<ParametrizedCategoryValue> = getDefaultState(word)
    ): Pair<WordSequence, Int> =
        speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?.apply(word, categoryValues.toSet())
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

    fun getDefaultState(word: Word): List<ParametrizedCategoryValue> =
        speechPartChangeParadigms[word.semanticsCore.speechPart]?.exponenceClusters
            ?.flatMap { it.categories }
            ?.filter { it.actualParametrizedValues.isNotEmpty() }
            ?.map { it.actualParametrizedValues[0] }//TODO another method for static categories swap
            ?.filter { v ->
                word.semanticsCore.staticCategories.none { it.parentClassName == v.categoryValue.parentClassName }
            }
            ?.union(word.semanticsCore.staticCategories.map { ParametrizedCategoryValue(it, CategorySource.SelfStated) })
            ?.toList()
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

    fun getSpeechPartParadigm(speechPart: SpeechPart) = speechPartChangeParadigms.getValue(speechPart)

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n")
}
