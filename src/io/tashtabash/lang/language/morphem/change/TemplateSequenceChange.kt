package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.lexis.Word


open class TemplateSequenceChange(val changes: List<TemplateChange>) : TemplateChange() {
    constructor(vararg changes: TemplateChange) : this(changes.toList())

    override val position: Position?
        get() {
            val allChanges = changes
                .map { it.position }
                .distinct()
            return if (allChanges.size == 1)
                allChanges[0]
            else null
        }

    override fun test(word: Word) = changes.any { it.test(word) }

    override fun change(word: Word, categoryValues: SourcedCategoryValues, derivationValues: List<DerivationClass>): Word {
        for (changeTemplate in changes) {
            val changedWord = changeTemplate.change(word, categoryValues, derivationValues)
            if (changedWord.toString() != word.toString())
                return changedWord
        }
        return word.copy()
    }

    fun findAppliedChangeIndex(word: Word, categoryValues: SourcedCategoryValues, derivationValues: List<DerivationClass>): Int? {
        for ((i, changeTemplate) in changes.withIndex()) {
            val changedWord = changeTemplate.change(word, categoryValues, derivationValues)
            if (changedWord.toString() != word.toString())
                return i
        }
        return null
    }

    override fun mirror() = TemplateSequenceChange(
        changes.map { it.mirror() }
    )

    override fun toString() =
        changes.joinToString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TemplateSequenceChange

        return changes == other.changes
    }

    override fun hashCode(): Int {
        return changes.hashCode()
    }
}
