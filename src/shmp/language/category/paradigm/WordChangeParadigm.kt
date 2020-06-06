package shmp.language.category.paradigm

import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.Word
import shmp.language.category.Category
import shmp.language.syntax.Clause

class WordChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryValues: List<ParametrizedCategoryValue> = getDefaultState(word)): Clause {
        val (startClause, wordPosition) = innerApply(word, categoryValues)
        val allWords = startClause.words.mapIndexed { i, w ->
            if (i == wordPosition || w == startClause[i]) listOf(w)
            else apply(w, categoryValues).words
        }.flatten()
        return Clause(allWords)
    }

    private fun innerApply(word: Word, categoryValues: List<ParametrizedCategoryValue> = getDefaultState(word)): Pair<Clause, Int> =
        speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?.apply(word, categoryValues.toSet())
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

    fun getDefaultState(word: Word): List<ParametrizedCategoryValue> =
        speechPartChangeParadigms[word.semanticsCore.speechPart]?.exponenceClusters
            ?.flatMap { it.categories }
            ?.filter { it.category.actualValues.isNotEmpty() }
            ?.map { it.category.actualValues[0] }//TODO another method for static categories swap
            ?.filter { enum ->
                word.semanticsCore.staticCategories.none { it.parentClassName == enum.parentClassName }
            }
            ?.union(word.semanticsCore.staticCategories)
            ?.map { ParametrizedCategoryValue(it) }
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

    fun getSpeechPartParadigm(speechPart: SpeechPart) = speechPartChangeParadigms.getValue(speechPart)

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n")
}
