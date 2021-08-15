package shmp.lang.language.morphem.change

import shmp.lang.language.lexis.Word


class TemplateSequenceChange(private val changes: List<WordChange>) : WordChange {
    constructor(vararg changes: WordChange) : this(changes.toList())

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

    override fun change(word: Word): Word {
        for (changeTemplate in changes) {
            val changedWord = changeTemplate.change(word)
            if (changedWord.toString() != word.toString())
                return changedWord
        }
        return word.copy()
    }

    override fun toString() = changes.joinToString()
}
