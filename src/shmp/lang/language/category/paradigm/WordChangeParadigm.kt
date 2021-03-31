package shmp.lang.language.category.paradigm

import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.Category
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence

class WordChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<SpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryValues: List<ParametrizedCategoryValue> = getDefaultState(word)): WordSequence {
        if (word.semanticsCore.speechPart == SpeechPart.DeixisPronoun) {
            val k = 0
        }
        val (startClause, wordPosition) = innerApply(word, categoryValues)
//        val allWords = startClause.words.mapIndexed { i, w ->
//            if (i == wordPosition || w == startClause[i]) listOf(w)
//            else apply(w, categoryValues).words
//        }.flatten()
//        return WordSequence(allWords)
        return startClause
    }

    private fun innerApply(
        word: Word,
        categoryValues: List<ParametrizedCategoryValue> = getDefaultState(word)
    ): Pair<WordSequence, Int> =
        speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?.apply(word, categoryValues.toSet())
            ?.handleNewWsWords()
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

    private fun Pair<WordSequence, Int>.handleNewWsWords(): Pair<WordSequence, Int> {
        val (ws, i) = this

        val newWs = ws.words.flatMapIndexed { j, w ->
            if (i == j || w.semanticsCore.speechPart == SpeechPart.Particle)
                listOf(w)
            else apply(
                w,
                ws[i].categoryValues.map {
                    ParametrizedCategoryValue(it.categoryValue, RelationGranted(SyntaxRelation.Subject))
                }
            ).words
        }

        return WordSequence(newWs) to i
    }

    fun getDefaultState(word: Word): List<ParametrizedCategoryValue> =
        speechPartChangeParadigms[word.semanticsCore.speechPart]?.exponenceClusters
            ?.flatMap { it.categories }
            ?.filter { it.actualParametrizedValues.isNotEmpty() }
            ?.map { it.actualParametrizedValues[0] }//TODO another method for static categories swap
            ?.filter { v ->
                word.semanticsCore.staticCategories.none { it.parentClassName == v.categoryValue.parentClassName }
            }
            ?.union(word.semanticsCore.staticCategories.map { ParametrizedCategoryValue(it, SelfStated) })
            ?.toList()
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")

    fun getSpeechPartParadigm(speechPart: SpeechPart) = speechPartChangeParadigms.getValue(speechPart)

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n")
}
