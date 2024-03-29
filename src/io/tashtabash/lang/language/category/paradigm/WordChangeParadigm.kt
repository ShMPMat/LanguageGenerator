package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.Category
import io.tashtabash.lang.language.category.CategorySource.*
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType


data class WordChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<TypedSpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(
        word: Word,
        latchType: LatchType = LatchType.Center,
        categoryValues: List<SourcedCategoryValue> = getDefaultState(word)
    ): FoldedWordSequence {
        val (startClause, _) = innerApply(word, latchType, categoryValues)

        return startClause
    }

    private fun innerApply(
        word: Word,
        latchType: LatchType,
        categoryValues: List<SourcedCategoryValue>
    ): Pair<FoldedWordSequence, Int> {
        val simpleCategoryValues = categoryValues.map { it.categoryValue }
        val applicableValues = categoryValues
            .filter { it.parent.compulsoryData.isApplicable(simpleCategoryValues) }
            .toSet()

        return speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?.apply(word, latchType, applicableValues)
            ?.handleNewWsWords(applicableValues)
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")
    }

    private fun Pair<FoldedWordSequence, Int>.handleNewWsWords(
        values: Set<SourcedCategoryValue>
    ): Pair<FoldedWordSequence, Int> {
        val (ws, i) = this

        val newWs = ws.words.flatMapIndexed { j, (w, l) ->
            if (i != j && w.semanticsCore.speechPart.type != SpeechPart.Particle) {
                val isAdnominal = w.semanticsCore.speechPart.subtype == adnominalSubtype
                val isArticle = w.semanticsCore.speechPart.type == SpeechPart.Article
                val newCv = if (isAdnominal || isArticle)
                    values.map {
                        SourcedCategoryValue(
                            it.categoryValue,
                            Agreement(SyntaxRelation.Agent, nominals),
                            it.parent
                        )
                    } else ws[i].first.categoryValues//.filter { it !in ws[i].categoryValues }

                apply(w, l, newCv).words
            } else listOf(w to l)
        }
        return FoldedWordSequence(newWs) to i
    }

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

    val speechParts = speechPartChangeParadigms.keys.sortedBy { it.type }

    override fun toString() = categories.joinToString("\n") + "\n\n" +
            speechPartChangeParadigms
                .map { it.value }
                .filter { it.hasChanges() }
                .joinToString("\n\n\n\n")
}
