package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.morphem.Affix
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence


class AffixCategoryApplicator(val affix: Affix, type: CategoryRealization?) : AbstractCategoryApplicator(type) {
    //TODO no guaranty for correctness
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { affix.change(it, values.toList(), listOf()) }

    override fun copy() =
        AffixCategoryApplicator(affix, type)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AffixCategoryApplicator

        return affix == other.affix
    }

    override fun hashCode(): Int =
        affix.hashCode()

    override fun toString() =
        affix.toString()
}
