package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.morphem.change.Position
import io.tashtabash.lang.language.morphem.change.TemplateChange


data class Prefix(override val templateChange: TemplateChange) : Affix {
    override val position = Position.Beginning

    init {
        if (templateChange.position != position)
            throw ExceptionInInitializerError("Prefix can change other parts of words")
    }

    override fun toString() = "Prefixes: $templateChange"
}
