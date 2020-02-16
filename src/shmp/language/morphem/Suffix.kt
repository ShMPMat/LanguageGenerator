package shmp.language.morphem

class Suffix(override val templateChange: TemplateWordChange) : Affix {
    init {
        if (templateChange.changes.any { it.position != Position.End })
            throw ExceptionInInitializerError("Suffix can change other parts of words")
    }

    override fun toString(): String {
        return "Suffixes: $templateChange"
    }


}