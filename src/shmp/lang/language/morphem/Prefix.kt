package shmp.lang.language.morphem

import shmp.lang.language.morphem.change.Position
import shmp.lang.language.morphem.change.TemplateSequenceChange


class Prefix(override val templateChange: TemplateSequenceChange) : Affix {
    override val position = Position.Beginning

    init {
        if (templateChange.position != position)
            throw ExceptionInInitializerError("Prefix can change other parts of words")
    }

    override fun toString() = "Prefixes: $templateChange"
}
