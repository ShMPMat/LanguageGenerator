package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.paradigm.SourcedCategoryValue


class NewWordCategoryApplicator(word: Word) : WordCategoryApplicator(word, CategoryRealization.NewWord) {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { w -> word.copyAndAddValues(w.categoryValues + values) }

    override fun copy() = NewWordCategoryApplicator(word)

    override fun toString() = "New word: $word"
}
