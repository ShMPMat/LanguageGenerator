package shmp.language.morphem

class Prefix(override val templateChange: TemplateWordChange) : Affix {
    override val position = Position.Beginning

    init {
        if (templateChange.position != position)
            throw ExceptionInInitializerError("Prefix can change other parts of words")
    }

    override fun toString(): String {
        return "Prefixes: $templateChange"
    }
}