package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.morphem.change.TemplateSequenceChange


class Suffix(override val templateChange: TemplateSequenceChange) : Affix {
    override val position = Position.End

    init {
        if (templateChange.position != position)
            throw ExceptionInInitializerError("Suffix can change other parts of words")
    }

    override fun toString() = "Suffixes: $templateChange"
}
