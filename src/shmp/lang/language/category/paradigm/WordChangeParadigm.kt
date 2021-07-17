package shmp.lang.language.category.paradigm

import shmp.lang.language.category.Category
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.InclusivityValue
import shmp.lang.language.lexis.*
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence


class WordChangeParadigm(
    val categories: List<Category>,
    private val speechPartChangeParadigms: Map<TypedSpeechPart, SpeechPartChangeParadigm>
) {
    fun apply(word: Word, categoryValues: List<SourcedCategoryValue> = getDefaultState(word)): WordSequence {
        val (startClause, _) = innerApply(word, categoryValues)

        return startClause
    }

    private fun innerApply(
        word: Word,
        categoryValues: List<SourcedCategoryValue>
    ): Pair<WordSequence, Int> {
        val simpleCategoryValues = categoryValues.map { it.categoryValue }
        val applicableValues = categoryValues
            .filter { it.parent.compulsoryData.isApplicable(simpleCategoryValues) }
            .toSet()

        return speechPartChangeParadigms[word.semanticsCore.speechPart]
            ?.apply(word, applicableValues)
            ?.handleNewWsWords()
            ?: throw ChangeException("No SpeechPartChangeParadigm for ${word.semanticsCore.speechPart}")
    }

    private fun Pair<WordSequence, Int>.handleNewWsWords(): Pair<WordSequence, Int> {
        val (ws, i) = this

        val newWs = ws.words.flatMapIndexed { j, w ->
            if (i != j && w.semanticsCore.speechPart.type != SpeechPart.Particle) {
                val isAdnominal = w.semanticsCore.speechPart.subtype == adnominalSubtype
                val isArticle = w.semanticsCore.speechPart.type == SpeechPart.Article
                val newCv = if (isAdnominal || isArticle)
                    ws[i].categoryValues.map {
                        SourcedCategoryValue(
                            it.categoryValue,
                            RelationGranted(SyntaxRelation.Agent, nominals),
                            it.parent
                        )
                    } else ws[i].categoryValues//.filter { it !in ws[i].categoryValues }

                apply(w, newCv).words
            } else listOf(w)
        }
        return WordSequence(newWs) to i
    }

    private fun getDefaultState(word: Word): List<SourcedCategoryValue> {
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
                SourcedCategoryValue(
                    v,
                    SelfStated,
                    paradigm.categories.first { it.category.outType == it.category.outType })
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
