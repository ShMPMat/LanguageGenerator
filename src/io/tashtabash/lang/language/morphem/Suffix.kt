package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.morphem.change.TemplateChange
import io.tashtabash.lang.language.morphem.change.TemplateSequenceChange


data class Suffix(override val templateChange: TemplateChange) : Affix {
    override val position = Position.End

    init {
        if (templateChange.position != position)
            throw ExceptionInInitializerError("Suffix can change other parts of words")
    }

    override fun toString() = "Suffixes: $templateChange"
}
