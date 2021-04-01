package shmp.lang.language.category.paradigm

import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.Category
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence


class WordChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<TypedSpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryValues: List<ParametrizedCategoryValue> = getDefaultState(word)): WordSequence {
        val (startClause, _) = innerApply(word, categoryValues)

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

    var cnt = 0
    private fun Pair<WordSequence, Int>.handleNewWsWords(): Pair<WordSequence, Int> {
        val (ws, i) = this

        val newWs = ws.words.flatMapIndexed { j, w ->
            if (i == j || w.semanticsCore.speechPart.type == SpeechPart.Particle)
                listOf(w)
            else {
                if (cnt > 5) {
                    val n = 0
                }
                cnt++
                apply(
                    w,
                    ws[i].categoryValues.map {
                        ParametrizedCategoryValue(it.categoryValue, RelationGranted(SyntaxRelation.Subject))
                    }
                ).words
            }
        }
        cnt = 0
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

    fun getSpeechPartParadigm(speechPart: TypedSpeechPart) = speechPartChangeParadigms.getValue(speechPart)
    fun getSpeechPartParadigms(speechPart: SpeechPart) = speechPartChangeParadigms
        .entries.filter { it.key.type == speechPart }
        .map { it.value }

    fun getSpeechParts(speechPart: SpeechPart) = speechPartChangeParadigms
        .keys.filter { it.type == speechPart }

    val speechParts = speechPartChangeParadigms.keys.sortedBy { it.type }

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n")
}
