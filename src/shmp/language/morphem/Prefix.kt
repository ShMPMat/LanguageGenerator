package shmp.language.morphem

class Prefix(override val templateChange: TemplateWordChange) : Affix {
    init {
        if (templateChange.changes.any { it.position != Position.Beginning })
            throw ExceptionInInitializerError("Prefix can change other parts of words")
    }

    override fun toString(): String {
        return "Prefixes: $templateChange"
    }
}